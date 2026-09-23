package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import com.fullprojectstudio.backend.service.EsportazioneCorsoService;
import com.fullprojectstudio.backend.service.QuoteService;
import com.fullprojectstudio.backend.service.StatisticheService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Genera il file Excel di un corso, lo rilegge e controlla fogli e contenuti principali.
 */
@ExtendWith(MockitoExtension.class)
class EsportazioneCorsoServiceTest {

    @Mock private StagioneRepository stagioneRepository;
    @Mock private CorsoRepository corsoRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private PresenzaRepository presenzaRepository;

    @Test
    void ilFileContieneTuttiIFogliConIscrittiEQuote() throws Exception {
        QuoteService quoteService = new QuoteService(iscrizioneRepository, pagamentoRepository, stagioneRepository);
        ReflectionTestUtils.setField(quoteService, "giornoScadenza", 7);
        StatisticheService statisticheService = new StatisticheService(stagioneRepository, corsoRepository, iscrizioneRepository, pagamentoRepository, quoteService);
        EsportazioneCorsoService esportazione = new EsportazioneCorsoService(corsoRepository, iscrizioneRepository, pagamentoRepository,
                presenzaRepository, quoteService, statisticheService);

        Stagione stagione = Stagione.builder().id(1L).nome("2026/2027").build();
        Corso corso = Corso.builder().id(1L).nome("Salsa").stagione(stagione).capienzaMax(20).prezzoMensile(new BigDecimal("60")).attivo(true).build();
        Studente anna = Studente.builder().id(1L).nome("Anna").cognome("Bianchi").sesso(Sesso.DONNA).telefono("3331112222").build();
        Studente marco = Studente.builder().id(2L).nome("Marco").cognome("Rossi").sesso(Sesso.UOMO).build();
        Iscrizione iscrAnna = Iscrizione.builder().id(1L).studente(anna).corso(corso).dataIscrizione(LocalDate.of(2026, 9, 1))
                .stato(StatoIscrizione.ATTIVA).ritiri(new ArrayList<>()).build();
        Iscrizione iscrMarco = Iscrizione.builder().id(2L).studente(marco).corso(corso).dataIscrizione(LocalDate.of(2026, 9, 1))
                .stato(StatoIscrizione.ATTIVA).ritiri(new ArrayList<>()).build();
        Pagamento pagamentoAnna = Pagamento.builder().iscrizione(iscrAnna).studente(anna).importo(new BigDecimal("60"))
                .dataPagamento(LocalDate.of(2026, 9, 3)).meseRiferimento(LocalDate.of(2026, 9, 1)).mesiCoperti(1)
                .metodo(MetodoPagamento.CONTANTI).stato(StatoPagamento.PAGATO).build();

        when(corsoRepository.findById(1L)).thenReturn(Optional.of(corso));
        when(iscrizioneRepository.findByCorsoId(1L)).thenReturn(List.of(iscrMarco, iscrAnna));
        when(pagamentoRepository.findByIscrizioneIdInAndStato(anyCollection(), any())).thenReturn(List.of(pagamentoAnna));
        when(stagioneRepository.findById(1L)).thenReturn(Optional.of(stagione));
        when(corsoRepository.findByStagioneId(1L)).thenReturn(List.of(corso));
        when(iscrizioneRepository.findByCorso_StagioneId(1L)).thenReturn(List.of(iscrMarco, iscrAnna));
        lenient().when(pagamentoRepository.findByIscrizioneIsNullAndStatoAndDataPagamentoBetween(any(), any(), any())).thenReturn(List.of());
        when(presenzaRepository.findByIscrizioneCorsoIdOrderByDataLezioneAsc(1L)).thenReturn(List.of(
                Presenza.builder().iscrizione(iscrAnna).dataLezione(LocalDate.of(2026, 9, 10)).presente(true).build()));

        EsportazioneCorsoService.FileEsportato file = esportazione.esporta(1L, LocalDate.of(2026, 9, 23));

        assertThat(file.nome()).isEqualTo("Corso Salsa - 2026-09-23.xlsx");
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(file.contenuto()))) {
            assertThat(IntStream.range(0, wb.getNumberOfSheets()).mapToObj(wb::getSheetName))
                    .containsExactly("Corso", "Iscritti", "Quote", "Pagamenti", "Presenze", "Statistiche");

            Sheet iscritti = wb.getSheet("Iscritti");
            assertThat(testo(iscritti.getRow(1), 0)).isEqualTo("Bianchi"); // ordine alfabetico per cognome
            assertThat(testo(iscritti.getRow(1), 6)).isEqualTo("3331112222");
            assertThat(testo(iscritti.getRow(2), 0)).isEqualTo("Rossi");
            assertThat(iscritti.getRow(2).getCell(13).getNumericCellValue()).isEqualTo(1); // settembre scaduto

            Sheet quote = wb.getSheet("Quote");
            assertThat(testo(quote.getRow(0), 2)).isEqualTo("Settembre 2026");
            assertThat(testo(quote.getRow(1), 2)).isEqualTo("Pagata");
            assertThat(testo(quote.getRow(2), 2)).isEqualTo("Scaduta");

            assertThat(testo(wb.getSheet("Presenze").getRow(1), 1)).isEqualTo("Bianchi Anna");
            assertThat(wb.getSheet("Statistiche").getRow(1).getCell(1).getNumericCellValue()).isEqualTo(2);
        }
    }

    private String testo(Row row, int colonna) {
        return row.getCell(colonna).getStringCellValue();
    }
}
