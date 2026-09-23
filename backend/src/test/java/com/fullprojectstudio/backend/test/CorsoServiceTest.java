package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.CorsoDto;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Istruttore;
import com.fullprojectstudio.backend.model.Livello;
import com.fullprojectstudio.backend.model.Sesso;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import com.fullprojectstudio.backend.model.StileBallo;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.IstruttoreRepository;
import com.fullprojectstudio.backend.repository.SalaRepository;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import com.fullprojectstudio.backend.service.CorsoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica il limite di 2 istruttori per corso in CorsoService.
 */
@ExtendWith(MockitoExtension.class)
class CorsoServiceTest {

    @Mock private CorsoRepository corsoRepository;
    @Mock private IstruttoreRepository istruttoreRepository;
    @Mock private SalaRepository salaRepository;
    @Mock private StagioneRepository stagioneRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;

    @InjectMocks private CorsoService corsoService;

    private Istruttore istruttore(long id) {
        return Istruttore.builder().id(id).nome("Nome" + id).cognome("Cognome" + id).build();
    }

    private CorsoDto dto(List<Long> istruttoriIds) {
        return CorsoDto.builder().nome("Bachata Base").stile(StileBallo.BACHATA)
                .livello(Livello.BASE).istruttoriIds(istruttoriIds).attivo(true).build();
    }

    @Test
    void rifiutaPiuDiDueIstruttori() {
        // Il controllo scatta prima di risolvere gli istruttori: nessuno stub necessario su istruttoreRepository.
        assertThatThrownBy(() -> corsoService.create(dto(List.of(1L, 2L, 3L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("massimo 2 istruttori");
    }

    @Test
    void accettaDueIstruttoriDistinti() {
        when(istruttoreRepository.findById(1L)).thenReturn(Optional.of(istruttore(1L)));
        when(istruttoreRepository.findById(2L)).thenReturn(Optional.of(istruttore(2L)));
        when(corsoRepository.save(any(Corso.class))).thenAnswer(inv -> inv.getArgument(0));

        CorsoDto risultato = corsoService.create(dto(List.of(1L, 2L)));

        assertThat(risultato.getIstruttoriNomi()).hasSize(2);
    }

    @Test
    void accettaUnSoloIstruttoreOppureNessuno() {
        when(corsoRepository.save(any(Corso.class))).thenAnswer(inv -> inv.getArgument(0));

        CorsoDto risultato = corsoService.create(dto(List.of()));

        assertThat(risultato.getIstruttoriNomi()).isEmpty();
    }

    @Test
    void popolaIlNumeroDiIscrittiAttivi() {
        when(corsoRepository.save(any(Corso.class))).thenAnswer(inv -> {
            Corso c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });
        when(iscrizioneRepository.countByCorsoIdAndStato(42L, StatoIscrizione.ATTIVA)).thenReturn(18L);

        CorsoDto risultato = corsoService.create(dto(List.of()));

        assertThat(risultato.getIscrittiAttivi()).isEqualTo(18);
    }

    @Test
    void popolaIlConteggioUominiDonneDegliIscrittiAttivi() {
        when(corsoRepository.save(any(Corso.class))).thenAnswer(inv -> {
            Corso c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });
        when(iscrizioneRepository.countByCorsoIdAndStatoAndStudente_Sesso(7L, StatoIscrizione.ATTIVA, Sesso.UOMO)).thenReturn(8L);
        when(iscrizioneRepository.countByCorsoIdAndStatoAndStudente_Sesso(7L, StatoIscrizione.ATTIVA, Sesso.DONNA)).thenReturn(12L);

        CorsoDto risultato = corsoService.create(dto(List.of()));

        assertThat(risultato.getIscrittiUomini()).isEqualTo(8);
        assertThat(risultato.getIscrittiDonne()).isEqualTo(12);
    }
}
