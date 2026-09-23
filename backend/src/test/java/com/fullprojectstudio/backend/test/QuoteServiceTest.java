package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto;
import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.model.TipoAbbonamento;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import com.fullprojectstudio.backend.service.QuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Verifica la regola delle quote mensili: pagamento entro il 7 del mese,
 * chi si iscrive dopo il 7 paga all'iscrizione, il trimestrale copre 3 mesi.
 */
@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    private static final YearMonth OTTOBRE = YearMonth.of(2026, 10);
    private static final long STAGIONE = 1L;

    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private StagioneRepository stagioneRepository;

    @InjectMocks private QuoteService quoteService;

    private final TipoAbbonamento mensile = TipoAbbonamento.builder().id(1L).nome("Mensile").durataGiorni(30).prezzo(new BigDecimal("60")).build();
    private final TipoAbbonamento trimestrale = TipoAbbonamento.builder().id(2L).nome("Trimestrale").durataGiorni(90).prezzo(new BigDecimal("160")).build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(quoteService, "giornoScadenza", 7);
    }

    private Iscrizione iscrizione(long id, TipoAbbonamento tipo, LocalDate dataIscrizione) {
        return Iscrizione.builder().id(id)
                .studente(Studente.builder().id(id).nome("Studente").cognome(String.valueOf(id)).build())
                .corso(Corso.builder().id(1L).nome("Salsa").prezzoMensile(new BigDecimal("60")).build())
                .tipoAbbonamento(tipo).dataIscrizione(dataIscrizione).stato(StatoIscrizione.ATTIVA).build();
    }

    private Pagamento pagamento(Iscrizione i, LocalDate data, YearMonth mese, int mesi) {
        return Pagamento.builder().id(100 + i.getId()).iscrizione(i).studente(i.getStudente())
                .importo(BigDecimal.TEN).dataPagamento(data).meseRiferimento(mese.atDay(1))
                .mesiCoperti(mesi).stato(StatoPagamento.PAGATO).build();
    }

    private void dati(List<Iscrizione> iscrizioni, List<Pagamento> pagamenti) {
        when(iscrizioneRepository.findByCorso_StagioneId(STAGIONE)).thenReturn(iscrizioni);
        lenient().when(pagamentoRepository.findByIscrizioneIdInAndStato(anyCollection(), any())).thenReturn(pagamenti);
    }

    private StatoQuota stato(YearMonth mese, LocalDate oggi) {
        List<QuotaIscrizioneDto> righe = quoteService.situazione(mese, STAGIONE, oggi).getRighe();
        assertThat(righe).hasSize(1);
        return righe.get(0).getStato();
    }

    @Test
    void pagatoEntroIlSetteEInTempo() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 7), OTTOBRE, 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 20))).isEqualTo(StatoQuota.PAGATO_IN_TEMPO);
    }

    @Test
    void pagatoDopoIlSetteEInRitardo() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 12), OTTOBRE, 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 20))).isEqualTo(StatoQuota.PAGATO_IN_RITARDO);
    }

    @Test
    void nonPagatoEDaPagareFinoAlSetteEPoiScaduto() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of());
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 7))).isEqualTo(StatoQuota.DA_PAGARE);
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 8))).isEqualTo(StatoQuota.NON_PAGATO);
    }

    @Test
    void ilPagamentoDiUnAltroMeseNonConta() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 9, 3), YearMonth.of(2026, 9), 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 20))).isEqualTo(StatoQuota.NON_PAGATO);
    }

    @Test
    void ilTrimestraleCopreTreMesiENonIlQuarto() {
        Iscrizione i = iscrizione(1, trimestrale, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 3), OTTOBRE, 3)));
        assertThat(stato(YearMonth.of(2026, 12), LocalDate.of(2026, 12, 20))).isEqualTo(StatoQuota.PAGATO_IN_TEMPO);
        assertThat(stato(YearMonth.of(2027, 1), LocalDate.of(2027, 1, 20))).isEqualTo(StatoQuota.NON_PAGATO);
    }

    @Test
    void chiSiIscriveDopoIlSettePagaAllIscrizione() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 10, 18));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 18), OTTOBRE, 1)));
        List<QuotaIscrizioneDto> righe = quoteService.situazione(OTTOBRE, STAGIONE, LocalDate.of(2026, 10, 20)).getRighe();
        assertThat(righe.get(0).getScadenza()).isEqualTo(LocalDate.of(2026, 10, 18));
        assertThat(righe.get(0).getStato()).isEqualTo(StatoQuota.PAGATO_IN_TEMPO);
    }

    @Test
    void escludeIscrizioniIniziateDopoIlMeseEOrdinaPerUrgenza() {
        Iscrizione pagata = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        Iscrizione nonPagata = iscrizione(2, mensile, LocalDate.of(2026, 9, 1));
        Iscrizione futura = iscrizione(3, mensile, LocalDate.of(2026, 11, 2));
        dati(List.of(pagata, nonPagata, futura), List.of(pagamento(pagata, LocalDate.of(2026, 10, 2), OTTOBRE, 1)));

        List<QuotaIscrizioneDto> righe = quoteService.situazione(OTTOBRE, STAGIONE, LocalDate.of(2026, 10, 20)).getRighe();

        assertThat(righe).extracting(QuotaIscrizioneDto::getIscrizioneId).containsExactly(2L, 1L);
        assertThat(righe).extracting(QuotaIscrizioneDto::getStato)
                .containsExactly(StatoQuota.NON_PAGATO, StatoQuota.PAGATO_IN_TEMPO);
    }
}
