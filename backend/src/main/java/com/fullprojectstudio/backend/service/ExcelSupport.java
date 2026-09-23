package com.fullprojectstudio.backend.service;

import org.apache.poi.ss.usermodel.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/** Funzioni comuni per i file Excel (backup notturno ed esportazione dei corsi). */
final class ExcelSupport {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ITALIANO = Locale.ITALIAN;

    private ExcelSupport() {
    }

    static CellStyle stileIntestazione(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    static CellStyle stileSfondo(Workbook wb, IndexedColors colore) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(colore.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    static Sheet nuovoSheet(Workbook wb, String nome, String[] intestazioni, CellStyle stileIntestazione) {
        Sheet sheet = wb.createSheet(nome);
        Row header = sheet.createRow(0);
        for (int i = 0; i < intestazioni.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(intestazioni[i]);
            cell.setCellStyle(stileIntestazione);
        }
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    static void larghezzaColonne(Sheet sheet, int numColonne) {
        for (int i = 0; i < numColonne; i++) {
            sheet.setColumnWidth(i, 22 * 256);
        }
    }

    static String formatta(LocalDate data) {
        return data != null ? data.format(DATA) : null;
    }

    // "Settembre 2026"
    static String formatta(YearMonth mese) {
        if (mese == null) return null;
        String nome = mese.getMonth().getDisplayName(TextStyle.FULL, ITALIANO);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1) + " " + mese.getYear();
    }

    // "Lunedì, Mercoledì"
    static String giorni(Collection<DayOfWeek> giorni) {
        return giorni.stream().sorted()
                .map(g -> { String n = g.getDisplayName(TextStyle.FULL, ITALIANO); return Character.toUpperCase(n.charAt(0)) + n.substring(1); })
                .collect(Collectors.joining(", "));
    }

    static Cell set(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        return cell;
    }
}
