package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.StatisticaMeseDto;
import com.fullprojectstudio.backend.dto.StatisticheDto;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.PeriodoRitiro;
import com.fullprojectstudio.backend.model.Sesso;
import com.fullprojectstudio.backend.model.Stagione;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import com.fullprojectstudio.backend.service.QuoteService;
import com.fullprojectstudio.backend.service.StatisticheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Verifica i numeri mese per mese della sezione Statistiche su uno scenario calcolato a mano.
 */
@ExtendWith(MockitoExtension.class)
class StatisticheServiceTest {

    private static final LocalDate OGGI = LocalDate.of(2026, 10, 20);

    @Mock private StagioneRepository stagioneRepository;
    @Mock private CorsoRepository corsoRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private PagamentoRepository pagamentoRepository;

    private StatisticheService statisticheService;

    private final Stagione stagione = Stagione.builder().id(1L).nome("2026/2027").corrente(true).build();
    private final Corso salsa = Corso.builder().id(1L).nome("Salsa").capienzaMax(20).prezzoMensile(new BigDecimal("60")).attivo(true).build();
    private final Corso bachata = Corso.builder().id(2L).nome("Bachata").prezzoMensile(new BigDecimal("60")).attivo(true).build();
    private final Studente marco = Studente.builder().id(1L).nome("Marco").cognome("R").sesso(Sesso.UOMO).build();
    private final Studente anna = Studente.builder().id(2L).nome("Anna").cognome("B").sesso(Sesso.DONNA).build();

    @BeforeEach
    void setUp() {
        QuoteService quoteService = new QuoteService(iscrizioneRepository, pagamentoRepository, stagioneRepository);
        ReflectionTestUtils.setField(quoteService, "giornoScadenza", 7);
        statisticheService = new StatisticheService(stagioneRepository, corsoRepository, iscrizioneRepository, pagamentoRepository, quoteService);
    }

    private Iscrizione iscrizione(long id, Studente s, Corso c, LocalDate data) {
        return Iscrizione.builder().id(id).studente(s).corso(c).dataIscrizione(data)
                .stato(StatoIscrizione.ATTIVA).ritiri(new ArrayList<>()).build();
    }

    private Pagamento pagamento(Iscrizione i, LocalDate data, YearMonth mese) {
        return Pagamento.builder().iscrizione(i).studente(i.getStudente()).importo(new BigDecimal("60"))
                .dataPagamento(data).meseRiferimento(mese.atDay(1)).mesiCoperti(1).stato(StatoPagamento.PAGATO).build();
    }

    private StatisticheDto calcola() {
        Iscrizione marcoSalsa = iscrizione(1, marco, salsa, LocalDate.of(2026, 9, 1));
        Iscrizione annaSalsa = iscrizione(2, anna, salsa, LocalDate.of(2026, 9, 10));
        annaSalsa.setStato(StatoIscrizione.RITIRATO);
        annaSalsa.getRitiri().add(PeriodoRitiro.builder().dataRitiro(LocalDate.of(2026, 10, 3)).build());
        Iscrizione marcoBachata = iscrizione(3, marco, bachata, LocalDate.of(2026, 8, 20));

        when(stagioneRepository.findByCorrenteTrue()).thenReturn(Optional.of(stagione));
        when(corsoRepository.findByStagioneId(1L)).thenReturn(List.of(salsa, bachata));
        when(iscrizioneRepository.findByCorso_StagioneId(1L)).thenReturn(List.of(marcoSalsa, annaSalsa, marcoBachata));
        when(pagamentoRepository.findByIscrizioneIdInAndStato(anyCollection(), any())).thenReturn(List.of(
                pagamento(marcoSalsa, LocalDate.of(2026, 9, 3), YearMonth.of(2026, 9)),
                pagamento(marcoSalsa, LocalDate.of(2026, 10, 10), YearMonth.of(2026, 10)),
                pagamento(annaSalsa, LocalDate.of(2026, 9, 10), YearMonth.of(2026, 9))));
        lenient().when(pagamentoRepository.findByIscrizioneIsNullAndStatoAndDataPagamentoBetween(eq(StatoPagamento.PAGATO),
                eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31))))
                .thenReturn(List.of(Pagamento.builder().importo(new BigDecimal("20")).dataPagamento(LocalDate.of(2026, 10, 5)).stato(StatoPagamento.PAGATO).build()));

        return statisticheService.statistiche(null, OGGI);
    }

    @Test
    void iMesiVannoDallaPrimaIscrizioneAlMeseCorrente() {
        assertThat(calcola().getScuola()).extracting(StatisticaMeseDto::getMese)
                .containsExactly(YearMonth.of(2026, 8), YearMonth.of(2026, 9), YearMonth.of(2026, 10));
    }

    @Test
    void numeriDellaScuolaMesePerMese() {
        List<StatisticaMeseDto> scuola = calcola().getScuola();
        StatisticaMeseDto agosto = scuola.get(0), settembre = scuola.get(1), ottobre = scuola.get(2);

        assertThat(agosto.getIscrittiAttivi()).isEqualTo(1);
        assertThat(agosto.getQuoteScadute()).isEqualTo(1);

        assertThat(settembre.getIscrittiAttivi()).isEqualTo(3);
        assertThat(settembre.getStudentiAttivi()).isEqualTo(2);
        assertThat(settembre.getUomini()).isEqualTo(2);
        assertThat(settembre.getDonne()).isEqualTo(1);
        assertThat(settembre.getNuoveIscrizioni()).isEqualTo(2);
        assertThat(settembre.getQuotePagateInTempo()).isEqualTo(2);
        assertThat(settembre.getQuoteScadute()).isEqualTo(1);
        assertThat(settembre.getIncassi()).isEqualByComparingTo("120");

        // Anna si è ritirata il 3 ottobre (entro il 7): ottobre non le è dovuto
        assertThat(ottobre.getIscrittiAttivi()).isEqualTo(2);
        assertThat(ottobre.getStudentiAttivi()).isEqualTo(1);
        assertThat(ottobre.getRitiri()).isEqualTo(1);
        assertThat(ottobre.getQuotePagateInRitardo()).isEqualTo(1);
        assertThat(ottobre.getQuoteScadute()).isEqualTo(1);
        assertThat(ottobre.getIncassi()).isEqualByComparingTo("80"); // 60 quota + 20 incasso non legato a un corso
    }

    @Test
    void numeriPerCorso() {
        StatisticheDto.StatisticheCorsoDto corsoSalsa = calcola().getCorsi().stream()
                .filter(c -> c.getCorsoNome().equals("Salsa")).findFirst().orElseThrow();
        StatisticaMeseDto ottobre = corsoSalsa.getMesi().get(2);

        assertThat(ottobre.getIscrittiAttivi()).isEqualTo(1);
        assertThat(ottobre.getRitiri()).isEqualTo(1);
        assertThat(ottobre.getIncassi()).isEqualByComparingTo("60");
        assertThat(ottobre.getStudentiAttivi()).isNull();
    }

    @Test
    void laStagioneFinitaSiFermaAllaDataDiFine() {
        Stagione passata = Stagione.builder().id(9L).nome("2024/2025")
                .dataInizio(LocalDate.of(2024, 9, 1)).dataFine(LocalDate.of(2025, 6, 30)).build();
        when(stagioneRepository.findById(9L)).thenReturn(Optional.of(passata));

        StatisticheDto dto = statisticheService.statistiche(9L, OGGI);

        assertThat(dto.getScuola()).hasSize(10);
        assertThat(dto.getScuola().get(9).getMese()).isEqualTo(YearMonth.of(2025, 6));
    }
}
