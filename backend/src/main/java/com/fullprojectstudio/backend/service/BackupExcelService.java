package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Genera un backup Excel (anagrafiche complete + movimenti del mese corrente
 * e precedente) su disco, cosi la segreteria puo continuare a lavorare da
 * quel file anche se il gestionale non fosse raggiungibile.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BackupExcelService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StudenteRepository studenteRepository;
    private final IstruttoreRepository istruttoreRepository;
    private final SalaRepository salaRepository;
    private final TipoAbbonamentoRepository tipoAbbonamentoRepository;
    private final CorsoRepository corsoRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PresenzaRepository presenzaRepository;

    @Value("${app.backup.dir}")
    private String backupDir;

    public Path generaBackup() {
        LocalDate oggi = LocalDate.now();
        LocalDate inizioMeseCorrente = oggi.withDayOfMonth(1);
        LocalDate inizioPeriodo = inizioMeseCorrente.minusMonths(1);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = creaStileHeader(workbook);

            scriviStudenti(workbook, headerStyle);
            scriviIstruttori(workbook, headerStyle);
            scriviSale(workbook, headerStyle);
            scriviCorsi(workbook, headerStyle);
            scriviAbbonamenti(workbook, headerStyle);
            scriviIscrizioni(workbook, headerStyle, inizioPeriodo);
            scriviPagamenti(workbook, headerStyle, inizioPeriodo, oggi);
            scriviPresenze(workbook, headerStyle, inizioPeriodo, oggi);

            Path dir = Path.of(backupDir);
            Files.createDirectories(dir);

            String nomeFile = "backup-" + oggi.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            Path destinazione = dir.resolve(nomeFile);
            Path ultimo = dir.resolve("backup-ultimo.xlsx");

            try (var out = Files.newOutputStream(destinazione)) {
                workbook.write(out);
            }
            Files.copy(destinazione, ultimo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            log.info("Backup Excel generato: {}", destinazione.toAbsolutePath());
            return destinazione;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile generare il backup Excel: " + e.getMessage(), e);
        }
    }

    private void scriviStudenti(Workbook wb, CellStyle headerStyle) {
        String[] headers = {"ID", "Nome", "Cognome", "Codice fiscale", "Data nascita", "Telefono", "Email",
                "Indirizzo", "Contatto emergenza", "Note mediche", "Data iscrizione", "Attivo"};
        List<Studente> studenti = studenteRepository.findAll();
        Sheet sheet = nuovoSheet(wb, "Studenti", headers, headerStyle);
        int r = 1;
        for (Studente s : studenti) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, s.getId());
            set(row, c++, s.getNome());
            set(row, c++, s.getCognome());
            set(row, c++, s.getCodiceFiscale());
            set(row, c++, formatta(s.getDataNascita()));
            set(row, c++, s.getTelefono());
            set(row, c++, s.getEmail());
            set(row, c++, s.getIndirizzo());
            set(row, c++, s.getContattoEmergenza());
            set(row, c++, s.getNoteMediche());
            set(row, c++, formatta(s.getDataIscrizione()));
            set(row, c, s.isAttivo() ? "Si" : "No");
        }
        autoSize(sheet, headers.length);
    }

    private void scriviIstruttori(Workbook wb, CellStyle headerStyle) {
        String[] headers = {"ID", "Nome", "Cognome", "Telefono", "Email", "Specializzazioni", "Compenso orario", "Attivo"};
        List<Istruttore> istruttori = istruttoreRepository.findAll();
        Sheet sheet = nuovoSheet(wb, "Istruttori", headers, headerStyle);
        int r = 1;
        for (Istruttore i : istruttori) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, i.getId());
            set(row, c++, i.getNome());
            set(row, c++, i.getCognome());
            set(row, c++, i.getTelefono());
            set(row, c++, i.getEmail());
            set(row, c++, i.getSpecializzazioni().stream().map(Enum::name).collect(Collectors.joining(", ")));
            set(row, c++, i.getCompensoOrario());
            set(row, c, i.isAttivo() ? "Si" : "No");
        }
        autoSize(sheet, headers.length);
    }

    private void scriviSale(Workbook wb, CellStyle headerStyle) {
        String[] headers = {"ID", "Nome", "Capienza", "Note"};
        List<Sala> sale = salaRepository.findAll();
        Sheet sheet = nuovoSheet(wb, "Sale", headers, headerStyle);
        int r = 1;
        for (Sala s : sale) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, s.getId());
            set(row, c++, s.getNome());
            set(row, c++, s.getCapienza());
            set(row, c, s.getNote());
        }
        autoSize(sheet, headers.length);
    }

    private void scriviCorsi(Workbook wb, CellStyle headerStyle) {
        String[] headers = {"ID", "Nome", "Stile", "Livello", "Istruttore", "Sala", "Giorni", "Orario inizio",
                "Orario fine", "Capienza max", "Prezzo mensile", "Attivo"};
        List<Corso> corsi = corsoRepository.findAll();
        Sheet sheet = nuovoSheet(wb, "Corsi", headers, headerStyle);
        int r = 1;
        for (Corso co : corsi) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, co.getId());
            set(row, c++, co.getNome());
            set(row, c++, co.getStile() != null ? co.getStile().name() : null);
            set(row, c++, co.getLivello() != null ? co.getLivello().name() : null);
            set(row, c++, co.getIstruttori().stream()
                    .map(i -> i.getNome() + " " + i.getCognome())
                    .collect(Collectors.joining(" + ")));
            set(row, c++, co.getSala() != null ? co.getSala().getNome() : null);
            set(row, c++, co.getGiorniSettimana().stream().map(Enum::name).collect(Collectors.joining(", ")));
            set(row, c++, co.getOrarioInizio() != null ? co.getOrarioInizio().toString() : null);
            set(row, c++, co.getOrarioFine() != null ? co.getOrarioFine().toString() : null);
            set(row, c++, co.getCapienzaMax());
            set(row, c++, co.getPrezzoMensile());
            set(row, c, co.isAttivo() ? "Si" : "No");
        }
        autoSize(sheet, headers.length);
    }

    private void scriviAbbonamenti(Workbook wb, CellStyle headerStyle) {
        String[] headers = {"ID", "Nome", "Descrizione", "Durata (giorni)", "Numero lezioni", "Prezzo", "Attivo"};
        List<TipoAbbonamento> abbonamenti = tipoAbbonamentoRepository.findAll();
        Sheet sheet = nuovoSheet(wb, "Abbonamenti", headers, headerStyle);
        int r = 1;
        for (TipoAbbonamento a : abbonamenti) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, a.getId());
            set(row, c++, a.getNome());
            set(row, c++, a.getDescrizione());
            set(row, c++, a.getDurataGiorni());
            set(row, c++, a.getNumeroLezioni());
            set(row, c++, a.getPrezzo());
            set(row, c, a.isAttivo() ? "Si" : "No");
        }
        autoSize(sheet, headers.length);
    }

    private void scriviIscrizioni(Workbook wb, CellStyle headerStyle, LocalDate inizioPeriodo) {
        String[] headers = {"ID", "Studente", "Corso", "Abbonamento", "Data iscrizione", "Data scadenza", "Stato", "Note"};
        List<Iscrizione> iscrizioni = iscrizioneRepository.findAll().stream()
                .filter(i -> i.getStato() == StatoIscrizione.ATTIVA
                        || !i.getDataIscrizione().isBefore(inizioPeriodo)
                        || (i.getDataScadenza() != null && !i.getDataScadenza().isBefore(inizioPeriodo)))
                .collect(Collectors.toList());
        Sheet sheet = nuovoSheet(wb, "Iscrizioni", headers, headerStyle);
        int r = 1;
        for (Iscrizione i : iscrizioni) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, i.getId());
            set(row, c++, i.getStudente().getNome() + " " + i.getStudente().getCognome());
            set(row, c++, i.getCorso().getNome());
            set(row, c++, i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().getNome() : null);
            set(row, c++, formatta(i.getDataIscrizione()));
            set(row, c++, formatta(i.getDataScadenza()));
            set(row, c++, i.getStato() != null ? i.getStato().name() : null);
            set(row, c, i.getNote());
        }
        autoSize(sheet, headers.length);
    }

    private void scriviPagamenti(Workbook wb, CellStyle headerStyle, LocalDate inizioPeriodo, LocalDate oggi) {
        String[] headers = {"ID", "Studente", "Data", "Importo", "Metodo", "Causale", "Stato", "Note"};
        List<Pagamento> pagamenti = pagamentoRepository.findByDataPagamentoBetween(inizioPeriodo, oggi);
        Sheet sheet = nuovoSheet(wb, "Pagamenti", headers, headerStyle);
        int r = 1;
        for (Pagamento p : pagamenti) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, p.getId());
            set(row, c++, p.getStudente().getNome() + " " + p.getStudente().getCognome());
            set(row, c++, formatta(p.getDataPagamento()));
            set(row, c++, p.getImporto());
            set(row, c++, p.getMetodo() != null ? p.getMetodo().name() : null);
            set(row, c++, p.getCausale());
            set(row, c++, p.getStato() != null ? p.getStato().name() : null);
            set(row, c, p.getNote());
        }
        autoSize(sheet, headers.length);
    }

    private void scriviPresenze(Workbook wb, CellStyle headerStyle, LocalDate inizioPeriodo, LocalDate oggi) {
        String[] headers = {"ID", "Studente", "Corso", "Data lezione", "Presente", "Note"};
        List<Presenza> presenze = presenzaRepository.findByDataLezioneBetween(inizioPeriodo, oggi);
        Sheet sheet = nuovoSheet(wb, "Presenze", headers, headerStyle);
        int r = 1;
        for (Presenza p : presenze) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, p.getId());
            set(row, c++, p.getIscrizione().getStudente().getNome() + " " + p.getIscrizione().getStudente().getCognome());
            set(row, c++, p.getIscrizione().getCorso().getNome());
            set(row, c++, formatta(p.getDataLezione()));
            set(row, c++, p.isPresente() ? "Si" : "No");
            set(row, c, p.getNote());
        }
        autoSize(sheet, headers.length);
    }

    // ---- helper ----

    private Sheet nuovoSheet(Workbook wb, String nome, String[] headers, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet(nome);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        return sheet;
    }

    private CellStyle creaStileHeader(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autoSize(Sheet sheet, int numColonne) {
        for (int i = 0; i < numColonne; i++) {
            sheet.setColumnWidth(i, 22 * 256);
        }
    }

    private String formatta(LocalDate data) {
        return data != null ? data.format(DATA) : null;
    }

    private void set(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
