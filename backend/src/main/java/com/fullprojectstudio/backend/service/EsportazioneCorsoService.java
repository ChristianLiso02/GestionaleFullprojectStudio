package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.dto.StatisticaMeseDto;
import com.fullprojectstudio.backend.dto.StatisticheDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.PresenzaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fullprojectstudio.backend.service.ExcelSupport.*;

/**
 * Esporta in un file Excel tutto ciò che riguarda un corso: dati del corso, iscritti,
 * situazione delle quote mese per mese, pagamenti, presenze e statistiche.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EsportazioneCorsoService {

    public record FileEsportato(String nome, byte[] contenuto) {
    }

    private static final Map<StatoQuota, String> ETICHETTE_QUOTA = Map.of(
            StatoQuota.PAGATO_IN_TEMPO, "Pagata",
            StatoQuota.PAGATO_IN_RITARDO, "In ritardo",
            StatoQuota.SCADUTO, "Scaduta",
            StatoQuota.DA_RINNOVARE, "Da rinnovare");

    private final CorsoRepository corsoRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PresenzaRepository presenzaRepository;
    private final QuoteService quoteService;
    private final StatisticheService statisticheService;

    public FileEsportato esporta(Long corsoId, LocalDate oggi) {
        Corso corso = corsoRepository.findById(corsoId)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato: " + corsoId));
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByCorsoId(corsoId).stream()
                .sorted(Comparator.comparing((Iscrizione i) -> i.getStato() == StatoIscrizione.RITIRATO)
                        .thenComparing(i -> i.getStudente().getCognome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(i -> i.getStudente().getNome(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<Pagamento> pagamenti = iscrizioni.isEmpty() ? List.of() : pagamentoRepository
                .findByIscrizioneIdInAndStato(iscrizioni.stream().map(Iscrizione::getId).toList(), StatoPagamento.PAGATO);
        Map<Long, List<Pagamento>> pagamentiPerIscrizione = pagamenti.stream()
                .collect(Collectors.groupingBy(p -> p.getIscrizione().getId()));
        List<StatisticaMeseDto> statistiche = statisticheCorso(corso, oggi);
        List<YearMonth> mesi = statistiche.stream().map(StatisticaMeseDto::getMese).toList();

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle intestazione = stileIntestazione(wb);
            scriviCorso(wb, corso, statistiche, oggi);
            scriviIscritti(wb, intestazione, iscrizioni, pagamentiPerIscrizione, oggi);
            scriviQuote(wb, intestazione, iscrizioni, pagamentiPerIscrizione, mesi, oggi);
            scriviPagamenti(wb, intestazione, pagamenti);
            scriviPresenze(wb, intestazione, corsoId);
            scriviStatistiche(wb, intestazione, statistiche);
            wb.write(out);
            return new FileEsportato("Corso " + corso.getNome() + " - " + oggi + ".xlsx", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile generare il file del corso: " + e.getMessage(), e);
        }
    }

    private List<StatisticaMeseDto> statisticheCorso(Corso corso, LocalDate oggi) {
        if (corso.getStagione() == null) {
            return List.of();
        }
        StatisticheDto dto = statisticheService.statistiche(corso.getStagione().getId(), oggi);
        return dto.getCorsi().stream()
                .filter(c -> c.getCorsoId().equals(corso.getId()))
                .findFirst()
                .map(StatisticheDto.StatisticheCorsoDto::getMesi)
                .orElse(List.of());
    }

    private void scriviCorso(Workbook wb, Corso corso, List<StatisticaMeseDto> statistiche, LocalDate oggi) {
        Sheet sheet = wb.createSheet("Corso");
        CellStyle etichetta = wb.createCellStyle();
        Font grassetto = wb.createFont();
        grassetto.setBold(true);
        etichetta.setFont(grassetto);

        Optional<StatisticaMeseDto> ultimoMese = statistiche.isEmpty() ? Optional.empty() : Optional.of(statistiche.get(statistiche.size() - 1));
        Object[][] righe = {
                {"Corso", corso.getNome()},
                {"Stagione", corso.getStagione() != null ? corso.getStagione().getNome() : null},
                {"Stile", corso.getStile() != null ? corso.getStile().name() : null},
                {"Livello", corso.getLivello() != null ? corso.getLivello().name() : null},
                {"Istruttori", corso.getIstruttori().stream().map(i -> i.getNome() + " " + i.getCognome()).sorted().collect(Collectors.joining(" + "))},
                {"Sala", corso.getSala() != null ? corso.getSala().getNome() : null},
                {"Giorni", giorni(corso.getGiorniSettimana())},
                {"Orario", corso.getOrarioInizio() != null ? corso.getOrarioInizio() + " - " + corso.getOrarioFine() : null},
                {"Capienza massima", corso.getCapienzaMax()},
                {"Prezzo mensile", corso.getPrezzoMensile()},
                {"Stato del corso", corso.isAttivo() ? "Attivo" : "Non attivo"},
                {"Iscritti attivi (" + ultimoMese.map(m -> formatta(m.getMese())).orElse("-") + ")", ultimoMese.map(StatisticaMeseDto::getIscrittiAttivi).orElse(null)},
                {"Uomini / Donne", ultimoMese.map(m -> m.getUomini() + " / " + m.getDonne()).orElse(null)},
                {"File generato il", formatta(oggi)}
        };
        for (int r = 0; r < righe.length; r++) {
            Row row = sheet.createRow(r);
            set(row, 0, righe[r][0]).setCellStyle(etichetta);
            set(row, 1, righe[r][1]);
        }
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 40 * 256);
    }

    private void scriviIscritti(Workbook wb, CellStyle intestazione, List<Iscrizione> iscrizioni,
                                Map<Long, List<Pagamento>> pagamenti, LocalDate oggi) {
        String[] colonne = {"Cognome", "Nome", "Sesso", "Stato iscrizione", "Data iscrizione", "Abbonamento", "Telefono", "Email",
                "Codice fiscale", "Data di nascita", "Contatto emergenza", "Note mediche", "Periodi di ritiro", "Quote scadute", "Note iscrizione"};
        Sheet sheet = nuovoSheet(wb, "Iscritti", colonne, intestazione);
        int r = 1;
        for (Iscrizione i : iscrizioni) {
            Studente s = i.getStudente();
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, s.getCognome());
            set(row, c++, s.getNome());
            set(row, c++, s.getSesso() == Sesso.UOMO ? "Uomo" : s.getSesso() == Sesso.DONNA ? "Donna" : null);
            set(row, c++, i.getStato() == StatoIscrizione.RITIRATO ? "Ritirato" : "Attiva");
            set(row, c++, formatta(i.getDataIscrizione()));
            set(row, c++, i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().getNome() : null);
            set(row, c++, s.getTelefono());
            set(row, c++, s.getEmail());
            set(row, c++, s.getCodiceFiscale());
            set(row, c++, formatta(s.getDataNascita()));
            set(row, c++, s.getContattoEmergenza());
            set(row, c++, s.getNoteMediche());
            set(row, c++, i.getRitiri().stream()
                    .map(p -> "dal " + formatta(p.getDataRitiro()) + (p.getDataRientro() != null ? " al " + formatta(p.getDataRientro()) : " (in corso)"))
                    .collect(Collectors.joining("; ")));
            set(row, c++, contaScadute(i, pagamenti.getOrDefault(i.getId(), List.of()), oggi));
            set(row, c, i.getNote());
        }
        larghezzaColonne(sheet, colonne.length);
    }

    private long contaScadute(Iscrizione i, List<Pagamento> pagamenti, LocalDate oggi) {
        if (i.getDataIscrizione() == null) return 0;
        long scadute = 0;
        for (YearMonth m = YearMonth.from(i.getDataIscrizione()); !m.isAfter(YearMonth.from(oggi)); m = m.plusMonths(1)) {
            if (quoteService.statoQuotaMese(i, m, oggi, pagamenti).filter(s -> s == StatoQuota.SCADUTO).isPresent()) scadute++;
        }
        return scadute;
    }

    // Griglia studenti × mesi con lo stato della quota, colorata come nel gestionale.
    private void scriviQuote(Workbook wb, CellStyle intestazione, List<Iscrizione> iscrizioni,
                             Map<Long, List<Pagamento>> pagamenti, List<YearMonth> mesi, LocalDate oggi) {
        String[] colonne = new String[mesi.size() + 2];
        colonne[0] = "Studente";
        colonne[1] = "Stato iscrizione";
        for (int k = 0; k < mesi.size(); k++) colonne[k + 2] = formatta(mesi.get(k));
        Sheet sheet = nuovoSheet(wb, "Quote", colonne, intestazione);
        sheet.createFreezePane(1, 1);

        Map<StatoQuota, CellStyle> stili = new EnumMap<>(StatoQuota.class);
        stili.put(StatoQuota.PAGATO_IN_TEMPO, stileSfondo(wb, IndexedColors.LIGHT_GREEN));
        stili.put(StatoQuota.PAGATO_IN_RITARDO, stileSfondo(wb, IndexedColors.LIGHT_YELLOW));
        stili.put(StatoQuota.SCADUTO, stileSfondo(wb, IndexedColors.ROSE));
        stili.put(StatoQuota.DA_RINNOVARE, stileSfondo(wb, IndexedColors.LIGHT_TURQUOISE));

        int r = 1;
        for (Iscrizione i : iscrizioni) {
            Row row = sheet.createRow(r++);
            set(row, 0, i.getStudente().getCognome() + " " + i.getStudente().getNome());
            set(row, 1, i.getStato() == StatoIscrizione.RITIRATO ? "Ritirato" : "Attiva");
            List<Pagamento> pagamentiIscrizione = pagamenti.getOrDefault(i.getId(), List.of());
            for (int k = 0; k < mesi.size(); k++) {
                Optional<StatoQuota> stato = quoteService.statoQuotaMese(i, mesi.get(k), oggi, pagamentiIscrizione);
                if (stato.isPresent()) {
                    set(row, k + 2, ETICHETTE_QUOTA.get(stato.get())).setCellStyle(stili.get(stato.get()));
                }
            }
        }
        r++;
        set(sheet.createRow(r++), 0, "Legenda");
        for (StatoQuota stato : List.of(StatoQuota.PAGATO_IN_TEMPO, StatoQuota.PAGATO_IN_RITARDO, StatoQuota.SCADUTO, StatoQuota.DA_RINNOVARE)) {
            set(sheet.createRow(r++), 0, ETICHETTE_QUOTA.get(stato)).setCellStyle(stili.get(stato));
        }
        set(sheet.createRow(r), 0, "Cella vuota = quota non dovuta (non ancora iscritto o ritirato)");
        larghezzaColonne(sheet, colonne.length);
        sheet.setColumnWidth(0, 28 * 256);
    }

    private void scriviPagamenti(Workbook wb, CellStyle intestazione, List<Pagamento> pagamenti) {
        String[] colonne = {"Data", "Studente", "Mese di riferimento", "Mesi coperti", "Importo", "Metodo", "Causale", "Note"};
        Sheet sheet = nuovoSheet(wb, "Pagamenti", colonne, intestazione);
        int r = 1;
        for (Pagamento p : pagamenti.stream().sorted(Comparator.comparing(Pagamento::getDataPagamento)).toList()) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, formatta(p.getDataPagamento()));
            set(row, c++, p.getStudente().getCognome() + " " + p.getStudente().getNome());
            set(row, c++, p.getMeseRiferimento() != null ? formatta(YearMonth.from(p.getMeseRiferimento())) : null);
            set(row, c++, p.getMesiCoperti() != null ? p.getMesiCoperti() : 1);
            set(row, c++, p.getImporto());
            set(row, c++, p.getMetodo() != null ? p.getMetodo().name() : null);
            set(row, c++, p.getCausale());
            set(row, c, p.getNote());
        }
        Row totale = sheet.createRow(r + 1);
        set(totale, 3, "Totale");
        set(totale, 4, pagamenti.stream().map(Pagamento::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add));
        larghezzaColonne(sheet, colonne.length);
    }

    private void scriviPresenze(Workbook wb, CellStyle intestazione, Long corsoId) {
        String[] colonne = {"Data lezione", "Studente", "Presente", "Note"};
        Sheet sheet = nuovoSheet(wb, "Presenze", colonne, intestazione);
        int r = 1;
        for (Presenza p : presenzaRepository.findByIscrizioneCorsoIdOrderByDataLezioneAsc(corsoId)) {
            Row row = sheet.createRow(r++);
            set(row, 0, formatta(p.getDataLezione()));
            set(row, 1, p.getIscrizione().getStudente().getCognome() + " " + p.getIscrizione().getStudente().getNome());
            set(row, 2, p.isPresente() ? "Si" : "No");
            set(row, 3, p.getNote());
        }
        larghezzaColonne(sheet, colonne.length);
    }

    private void scriviStatistiche(Workbook wb, CellStyle intestazione, List<StatisticaMeseDto> statistiche) {
        String[] colonne = {"Mese", "Iscritti attivi", "Uomini", "Donne", "Nuove iscrizioni", "Ritiri", "Rientri", "Incassi",
                "Quote in tempo", "Quote in ritardo", "Quote scadute", "Quote da rinnovare"};
        Sheet sheet = nuovoSheet(wb, "Statistiche", colonne, intestazione);
        int r = 1;
        for (StatisticaMeseDto m : statistiche) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, formatta(m.getMese()));
            set(row, c++, m.getIscrittiAttivi());
            set(row, c++, m.getUomini());
            set(row, c++, m.getDonne());
            set(row, c++, m.getNuoveIscrizioni());
            set(row, c++, m.getRitiri());
            set(row, c++, m.getRientri());
            set(row, c++, m.getIncassi());
            set(row, c++, m.getQuotePagateInTempo());
            set(row, c++, m.getQuotePagateInRitardo());
            set(row, c++, m.getQuoteScadute());
            set(row, c, m.getQuoteDaRinnovare());
        }
        larghezzaColonne(sheet, colonne.length);
    }
}
