package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.PagamentoDto;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.MetodoPagamento;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.model.TipoPagamento;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import com.fullprojectstudio.backend.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * La lezione singola è legata al corso e al giorno della lezione, mai a un'iscrizione:
 * così non crea quote mensili ma conta negli incassi del corso.
 */
@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private StudenteRepository studenteRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private CorsoRepository corsoRepository;

    @InjectMocks private PagamentoService pagamentoService;

    private final Studente studente = Studente.builder().id(1L).nome("Luca").cognome("Neri").build();
    private final Corso salsa = Corso.builder().id(3L).nome("Salsa Base").build();

    @BeforeEach
    void setUp() {
        when(studenteRepository.findById(1L)).thenReturn(Optional.of(studente));
    }

    private PagamentoDto lezioneSingola() {
        return PagamentoDto.builder().tipo(TipoPagamento.LEZIONE_SINGOLA).studenteId(1L).corsoId(3L)
                .dataLezione(LocalDate.of(2026, 10, 15)).importo(new BigDecimal("15")).metodo(MetodoPagamento.CONTANTI)
                .meseRiferimento(YearMonth.of(2026, 10)).build();
    }

    @Test
    void laLezioneSingolaERegistrataSulCorsoESenzaIscrizione() {
        when(corsoRepository.findById(3L)).thenReturn(Optional.of(salsa));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        PagamentoDto dto = lezioneSingola();
        dto.setIscrizioneId(99L);
        dto.setMesiCoperti(3);

        PagamentoDto risultato = pagamentoService.create(dto);

        assertThat(risultato.getTipo()).isEqualTo(TipoPagamento.LEZIONE_SINGOLA);
        assertThat(risultato.getIscrizioneId()).isNull();
        assertThat(risultato.getCorsoId()).isEqualTo(3L);
        assertThat(risultato.getCorsoNome()).isEqualTo("Salsa Base");
        assertThat(risultato.getDataLezione()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(risultato.getMesiCoperti()).isEqualTo(1);
    }

    @Test
    void ilMeseDiRiferimentoEQuelloDellaLezione() {
        when(corsoRepository.findById(3L)).thenReturn(Optional.of(salsa));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        PagamentoDto dto = lezioneSingola();
        dto.setMeseRiferimento(YearMonth.of(2026, 9));

        assertThat(pagamentoService.create(dto).getMeseRiferimento()).isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    void servonoCorsoEGiornoDellaLezione() {
        PagamentoDto senzaCorso = lezioneSingola();
        senzaCorso.setCorsoId(null);
        assertThatThrownBy(() -> pagamentoService.create(senzaCorso))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("corso");

        PagamentoDto senzaGiorno = lezioneSingola();
        senzaGiorno.setDataLezione(null);
        assertThatThrownBy(() -> pagamentoService.create(senzaGiorno))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("giorno");
    }

    @Test
    void unaQuotaNonHaCorsoNeGiornoDiLezione() {
        Iscrizione iscrizione = Iscrizione.builder().id(5L).studente(studente).corso(salsa).build();
        when(iscrizioneRepository.findById(5L)).thenReturn(Optional.of(iscrizione));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        PagamentoDto dto = PagamentoDto.builder().studenteId(1L).iscrizioneId(5L).dataLezione(LocalDate.of(2026, 10, 15))
                .importo(new BigDecimal("60")).metodo(MetodoPagamento.CONTANTI).meseRiferimento(YearMonth.of(2026, 10)).mesiCoperti(1).build();

        PagamentoDto risultato = pagamentoService.create(dto);

        assertThat(risultato.getTipo()).isEqualTo(TipoPagamento.QUOTA_CORSO);
        assertThat(risultato.getIscrizioneId()).isEqualTo(5L);
        assertThat(risultato.getDataLezione()).isNull();
        // Il corso della quota è quello dell'iscrizione (solo in lettura, per le liste).
        assertThat(risultato.getCorsoNome()).isEqualTo("Salsa Base");
    }
}
