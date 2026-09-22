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

    @Test
    void calcolaLaDataDiScadenzaDalTipoAbbonamento() {
        TipoAbbonamento abbonamento = TipoAbbonamento.builder().id(5L).nome("Mensile")
                .durataGiorni(30).build();
        IscrizioneDto dto = dto(10L, 1L);
        dto.setTipoAbbonamentoId(5L);
        dto.setDataIscrizione(LocalDate.of(2026, 1, 1));

        when(studenteRepository.findById(10L)).thenReturn(Optional.of(studente(10L)));
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso(1L, null)));
        when(tipoAbbonamentoRepository.findById(5L)).thenReturn(Optional.of(abbonamento));
        when(iscrizioneRepository.save(any(Iscrizione.class))).thenAnswer(inv -> inv.getArgument(0));

        IscrizioneDto risultato = iscrizioneService.create(dto);

        assertThat(risultato.getDataScadenza()).isEqualTo(LocalDate.of(2026, 1, 31));
    }
}
