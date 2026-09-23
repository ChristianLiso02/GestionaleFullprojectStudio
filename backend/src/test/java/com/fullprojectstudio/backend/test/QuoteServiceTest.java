package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto;
import com.fullprojectstudio.backend.dto.QuotaScadutaDto;
import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.PeriodoRitiro;
import com.fullprojectstudio.backend.model.Stagione;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        ReflectionTestUtils.setField(quoteService, "mesiScadutiDaVerificare", 1);
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
    void daRinnovareFinoAlSetteEPoiScaduto() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of());
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 7))).isEqualTo(StatoQuota.DA_RINNOVARE);
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 8))).isEqualTo(StatoQuota.SCADUTO);
    }

    @Test
    void ilPagamentoDiUnAltroMeseNonConta() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 9, 3), YearMonth.of(2026, 9), 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 20))).isEqualTo(StatoQuota.SCADUTO);
    }

    @Test
    void ilTrimestraleCopreTreMesiENonIlQuarto() {
        Iscrizione i = iscrizione(1, trimestrale, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 3), OTTOBRE, 3)));
        assertThat(stato(YearMonth.of(2026, 12), LocalDate.of(2026, 12, 20))).isEqualTo(StatoQuota.PAGATO_IN_TEMPO);
        assertThat(stato(YearMonth.of(2027, 1), LocalDate.of(2027, 1, 20))).isEqualTo(StatoQuota.SCADUTO);
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
                .containsExactly(StatoQuota.SCADUTO, StatoQuota.PAGATO_IN_TEMPO);
    }

    private PeriodoRitiro periodo(LocalDate ritiro, LocalDate rientro) {
        return PeriodoRitiro.builder().dataRitiro(ritiro).dataRientro(rientro).build();
    }

    private Iscrizione conRitiri(PeriodoRitiro... periodi) {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        i.setRitiri(new ArrayList<>(List.of(periodi)));
        boolean inCorso = List.of(periodi).stream().anyMatch(p -> p.getDataRientro() == null);
        i.setStato(inCorso ? StatoIscrizione.RITIRATO : StatoIscrizione.ATTIVA);
        return i;
    }

    private boolean dovuta(YearMonth mese, LocalDate oggi) {
        return !quoteService.situazione(mese, STAGIONE, oggi).getRighe().isEmpty();
    }

    @Test
    void chiVieneIlGiovediSeiERinnovaEInRegola() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of());
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 6))).isEqualTo(StatoQuota.DA_RINNOVARE);

        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 6), OTTOBRE, 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 6))).isEqualTo(StatoQuota.PAGATO_IN_TEMPO);
    }

    @Test
    void unaQuotaScadutaNonRitiraLoStudenteEPuoPagareInRitardo() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        dati(List.of(i), List.of());
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 20))).isEqualTo(StatoQuota.SCADUTO);
        assertThat(i.getStato()).isEqualTo(StatoIscrizione.ATTIVA);
        assertThat(dovuta(YearMonth.of(2026, 11), LocalDate.of(2026, 11, 3))).isTrue();

        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 10, 25), OTTOBRE, 1)));
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 10, 26))).isEqualTo(StatoQuota.PAGATO_IN_RITARDO);
    }

    @Test
    void chiSiRitiraEntroIlSetteNonDeveQuelMese() {
        dati(List.of(conRitiri(periodo(LocalDate.of(2026, 11, 3), null))), List.of());
        assertThat(stato(OTTOBRE, LocalDate.of(2026, 11, 20))).isEqualTo(StatoQuota.SCADUTO);
        assertThat(dovuta(YearMonth.of(2026, 11), LocalDate.of(2026, 11, 20))).isFalse();
    }

    @Test
    void chiSiRitiraDopoIlSetteDeveQuelMeseMaNonIlSuccessivo() {
        dati(List.of(conRitiri(periodo(LocalDate.of(2026, 11, 20), null))), List.of());
        assertThat(stato(YearMonth.of(2026, 11), LocalDate.of(2026, 11, 25))).isEqualTo(StatoQuota.SCADUTO);
        assertThat(dovuta(YearMonth.of(2026, 12), LocalDate.of(2026, 12, 20))).isFalse();
    }

    @Test
    void ritiratoTreMesiRicominciaAPagareDalMeseDelRientroSenzaSaldareIPrecedenti() {
        dati(List.of(conRitiri(periodo(LocalDate.of(2026, 11, 3), LocalDate.of(2027, 2, 2)))), List.of());

        assertThat(dovuta(YearMonth.of(2026, 11), LocalDate.of(2027, 2, 20))).isFalse();
        assertThat(dovuta(YearMonth.of(2026, 12), LocalDate.of(2027, 2, 20))).isFalse();
        assertThat(dovuta(YearMonth.of(2027, 1), LocalDate.of(2027, 2, 20))).isFalse();
        assertThat(stato(YearMonth.of(2027, 2), LocalDate.of(2027, 2, 5))).isEqualTo(StatoQuota.DA_RINNOVARE);
    }

    @Test
    void chiRientraDopoIlSetteRinnovaIlGiornoDelRientro() {
        dati(List.of(conRitiri(periodo(LocalDate.of(2026, 11, 3), LocalDate.of(2027, 1, 15)))), List.of());
        List<QuotaIscrizioneDto> gennaio = quoteService.situazione(YearMonth.of(2027, 1), STAGIONE, LocalDate.of(2027, 1, 10)).getRighe();
        assertThat(gennaio.get(0).getScadenza()).isEqualTo(LocalDate.of(2027, 1, 15));
        assertThat(gennaio.get(0).getStato()).isEqualTo(StatoQuota.DA_RINNOVARE);
    }

    @Test
    void piuPeriodiDiRitiroVengonoTuttiRispettati() {
        dati(List.of(conRitiri(
                periodo(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 12, 1)),
                periodo(LocalDate.of(2027, 2, 3), LocalDate.of(2027, 4, 1)))), List.of());
        LocalDate oggi = LocalDate.of(2027, 4, 20);

        assertThat(dovuta(YearMonth.of(2026, 10), oggi)).isFalse();
        assertThat(dovuta(YearMonth.of(2026, 11), oggi)).isFalse();
        assertThat(dovuta(YearMonth.of(2026, 12), oggi)).isTrue();
        assertThat(dovuta(YearMonth.of(2027, 1), oggi)).isTrue();
        assertThat(dovuta(YearMonth.of(2027, 2), oggi)).isFalse();
        assertThat(dovuta(YearMonth.of(2027, 3), oggi)).isFalse();
        assertThat(dovuta(YearMonth.of(2027, 4), oggi)).isTrue();
    }

    @Test
    void malattiaRegistrataDopoConDatePassateAzzeraIlMese() {
        dati(List.of(conRitiri(periodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1)))), List.of());
        assertThat(dovuta(OTTOBRE, LocalDate.of(2026, 11, 10))).isFalse();
        List<QuotaIscrizioneDto> novembre = quoteService.situazione(YearMonth.of(2026, 11), STAGIONE, LocalDate.of(2026, 11, 10)).getRighe();
        assertThat(novembre.get(0).getScadenza()).isEqualTo(LocalDate.of(2026, 11, 7));
    }

    @Test
    void iscrizioneRitirataPrimaDelloStoricoNonHaQuoteDovute() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 1));
        i.setStato(StatoIscrizione.RITIRATO);
        dati(List.of(i), List.of());
        assertThat(dovuta(OTTOBRE, LocalDate.of(2026, 10, 20))).isFalse();
    }

    // ---- quote scadute da verificare (ritiri taciti) ----

    private final YearMonth LUGLIO = YearMonth.of(2026, 7);

    private List<QuotaScadutaDto> daVerificare(List<Iscrizione> iscrizioni, List<Pagamento> pagamenti, LocalDate oggi) {
        when(stagioneRepository.findByCorrenteTrue()).thenReturn(Optional.of(Stagione.builder().id(STAGIONE).nome("2026/2027").build()));
        dati(iscrizioni, pagamenti);
        return quoteService.quoteScaduteDaVerificare(oggi);
    }

    @Test
    void chiSmetteDiPagareSenzaAvvisareVaVerificatoConDataProposta() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 7, 1));
        List<QuotaScadutaDto> lista = daVerificare(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 7, 3), LUGLIO, 1)), LocalDate.of(2026, 9, 23));

        assertThat(lista).singleElement().satisfies(q -> {
            assertThat(q.getMesiScaduti()).containsExactly(YearMonth.of(2026, 8), YearMonth.of(2026, 9));
            assertThat(q.getDataRitiroProposta()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(q.getUltimoPagamento()).isEqualTo(LocalDate.of(2026, 7, 3));
        });
    }

    @Test
    void ilMeseCorrenteDaRinnovareNonInterrompeLaSerie() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 7, 1));
        List<QuotaScadutaDto> lista = daVerificare(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 7, 3), LUGLIO, 1)), LocalDate.of(2026, 10, 3));

        assertThat(lista.get(0).getMesiScaduti()).containsExactly(YearMonth.of(2026, 8), YearMonth.of(2026, 9));
    }

    @Test
    void chiHaPagatoIlMeseCorrenteNonEUnPossibileRitiro() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 7, 1));
        List<QuotaScadutaDto> lista = daVerificare(List.of(i),
                List.of(pagamento(i, LocalDate.of(2026, 9, 2), YearMonth.of(2026, 9), 1)), LocalDate.of(2026, 9, 23));

        assertThat(lista).isEmpty();
    }

    @Test
    void laDataPropostaNonPrecedeLIscrizione() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 9, 15));
        List<QuotaScadutaDto> lista = daVerificare(List.of(i), List.of(), LocalDate.of(2026, 9, 23));

        assertThat(lista.get(0).getDataRitiroProposta()).isEqualTo(LocalDate.of(2026, 9, 15));
    }

    @Test
    void conSogliaDueMesiUnSoloMeseScadutoNonBasta() {
        ReflectionTestUtils.setField(quoteService, "mesiScadutiDaVerificare", 2);
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 8, 1));
        List<QuotaScadutaDto> lista = daVerificare(List.of(i),
                List.of(pagamento(i, LocalDate.of(2026, 8, 3), YearMonth.of(2026, 8), 1)), LocalDate.of(2026, 9, 23));

        assertThat(lista).isEmpty();
    }

    @Test
    void chiEGiaRitiratoNonVaVerificato() {
        Iscrizione i = conRitiri(periodo(LocalDate.of(2026, 9, 1), null));
        assertThat(daVerificare(List.of(i), List.of(), LocalDate.of(2026, 9, 23))).isEmpty();
    }

    @Test
    void laQuotaScadutaNellaPaginaDelMeseHaLaDataDiRitiroProposta() {
        Iscrizione i = iscrizione(1, mensile, LocalDate.of(2026, 7, 1));
        dati(List.of(i), List.of(pagamento(i, LocalDate.of(2026, 7, 3), LUGLIO, 1)));

        QuotaIscrizioneDto settembre = quoteService.situazione(YearMonth.of(2026, 9), STAGIONE, LocalDate.of(2026, 9, 23)).getRighe().get(0);

        assertThat(settembre.getStato()).isEqualTo(StatoQuota.SCADUTO);
        assertThat(settembre.getDataRitiroProposta()).isEqualTo(LocalDate.of(2026, 8, 1));
    }
}
