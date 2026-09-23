package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.IscrizioneDto;
import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import com.fullprojectstudio.backend.repository.TipoAbbonamentoRepository;
import com.fullprojectstudio.backend.service.IscrizioneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica la logica di IscrizioneService: limite di capienza del corso
 * e calcolo automatico della data di scadenza dal tipo di abbonamento.
 */
@ExtendWith(MockitoExtension.class)
class IscrizioneServiceTest {

    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private StudenteRepository studenteRepository;
    @Mock private CorsoRepository corsoRepository;
    @Mock private TipoAbbonamentoRepository tipoAbbonamentoRepository;

    @InjectMocks private IscrizioneService iscrizioneService;

    private Studente studente(long id) {
        return Studente.builder().id(id).nome("Mario").cognome("Rossi").build();
    }

    private Corso corso(long id, Integer capienzaMax) {
        return Corso.builder().id(id).nome("Salsa Cubana").stile(StileBallo.SALSA_CUBANA)
                .livello(Livello.BASE).capienzaMax(capienzaMax).attivo(true).build();
    }

    private IscrizioneDto dto(long studenteId, long corsoId) {
        return IscrizioneDto.builder().studenteId(studenteId).corsoId(corsoId)
                .stato(StatoIscrizione.ATTIVA).dataIscrizione(LocalDate.now()).build();
    }

    @Test
    void rifiutaIscrizioneOltreLaCapienzaMassima() {
        when(studenteRepository.findById(10L)).thenReturn(Optional.of(studente(10L)));
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso(1L, 2)));
        when(iscrizioneRepository.countByCorsoIdAndStato(1L, StatoIscrizione.ATTIVA)).thenReturn(2L);

        assertThatThrownBy(() -> iscrizioneService.create(dto(10L, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capienza massima");
    }

    @Test
    void accettaIscrizioneSottoLaCapienzaMassima() {
        when(studenteRepository.findById(10L)).thenReturn(Optional.of(studente(10L)));
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso(1L, 2)));
        when(iscrizioneRepository.countByCorsoIdAndStato(1L, StatoIscrizione.ATTIVA)).thenReturn(1L);
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.create(dto(10L, 1L));

        assertThat(risultato.getStato()).isEqualTo(StatoIscrizione.ATTIVA);
    }

    @Test
    void nessunLimiteSeCapienzaMassimaNonImpostata() {
        when(studenteRepository.findById(10L)).thenReturn(Optional.of(studente(10L)));
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso(1L, null)));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.create(dto(10L, 1L));

        assertThat(risultato).isNotNull();
    }

    private Iscrizione esistente(long id, Corso corso, StatoIscrizione stato) {
        return Iscrizione.builder().id(id).studente(studente(10L)).corso(corso)
                .dataIscrizione(LocalDate.of(2026, 9, 1)).stato(stato).build();
    }

    @Test
    void ritirareApreUnPeriodoDiRitiroConLaDataScelta() {
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(esistente(5L, corso(1L, null), StatoIscrizione.ATTIVA)));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.ritira(5L, LocalDate.of(2026, 9, 15));

        assertThat(risultato.getStato()).isEqualTo(StatoIscrizione.RITIRATO);
        assertThat(risultato.getDataRitiro()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(risultato.getRitiri()).hasSize(1);
    }

    @Test
    void nonSiPuoRitirareConUnaDataFutura() {
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(esistente(5L, corso(1L, null), StatoIscrizione.ATTIVA)));

        assertThatThrownBy(() -> iscrizioneService.ritira(5L, LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void riattivareChiudeIlPeriodoDiRitiro() {
        Iscrizione ritirata = esistente(5L, corso(1L, 10), StatoIscrizione.RITIRATO);
        ritirata.getRitiri().add(PeriodoRitiro.builder().id(1L).dataRitiro(LocalDate.of(2026, 9, 10)).build());
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(ritirata));
        when(iscrizioneRepository.countByCorsoIdAndStato(1L, StatoIscrizione.ATTIVA)).thenReturn(3L);
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.riattiva(5L, LocalDate.of(2026, 9, 20));

        assertThat(risultato.getStato()).isEqualTo(StatoIscrizione.ATTIVA);
        assertThat(risultato.getRitiri().get(0).getDataRientro()).isEqualTo(LocalDate.of(2026, 9, 20));
    }

    @Test
    void ilRientroNonPuoPrecedereIlRitiro() {
        Iscrizione ritirata = esistente(5L, corso(1L, null), StatoIscrizione.RITIRATO);
        ritirata.getRitiri().add(PeriodoRitiro.builder().id(1L).dataRitiro(LocalDate.of(2026, 9, 10)).build());
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(ritirata));

        assertThatThrownBy(() -> iscrizioneService.riattiva(5L, LocalDate.of(2026, 9, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precedente al ritiro");
    }

    @Test
    void riattivareUnaVecchiaIscrizioneSenzaStoricoCreaIlPeriodoDallIscrizione() {
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(esistente(5L, corso(1L, null), StatoIscrizione.RITIRATO)));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.riattiva(5L, LocalDate.of(2026, 9, 20));

        assertThat(risultato.getRitiri()).singleElement().satisfies(p -> {
            assertThat(p.getDataRitiro()).isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(p.getDataRientro()).isEqualTo(LocalDate.of(2026, 9, 20));
        });
    }

    @Test
    void nonSiRiattivaSeIlCorsoEPieno() {
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(esistente(5L, corso(1L, 2), StatoIscrizione.RITIRATO)));
        when(iscrizioneRepository.countByCorsoIdAndStato(1L, StatoIscrizione.ATTIVA)).thenReturn(2L);

        assertThatThrownBy(() -> iscrizioneService.riattiva(5L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capienza massima");
    }

    @Test
    void annullareUnRitiroInCorsoRiportaLIscrizioneAttiva() {
        Iscrizione ritirata = esistente(5L, corso(1L, null), StatoIscrizione.RITIRATO);
        ritirata.getRitiri().add(PeriodoRitiro.builder().id(9L).dataRitiro(LocalDate.of(2026, 9, 10)).build());
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(ritirata));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.annullaRitiro(5L, 9L);

        assertThat(risultato.getStato()).isEqualTo(StatoIscrizione.ATTIVA);
        assertThat(risultato.getRitiri()).isEmpty();
    }

    @Test
    void laModificaNonCambiaLoStatoDellIscrizione() {
        Corso corso = corso(1L, null);
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(esistente(5L, corso, StatoIscrizione.RITIRATO)));
        when(studenteRepository.findById(10L)).thenReturn(Optional.of(studente(10L)));
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.update(5L, dto(10L, 1L));

        assertThat(risultato.getStato()).isEqualTo(StatoIscrizione.RITIRATO);
    }
}
