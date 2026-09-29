package com.fullprojectstudio.backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Esporta tutti i dati del gestionale installato sul PC in un unico file, sempre con lo stesso nome e nella stessa
 * cartella (l'export precedente viene sostituito). È il file da portare sul server per trasferire i dati in
 * PostgreSQL con {@code --trasferisci-da-h2} (vedi INSTALLAZIONE-SERVER.md).
 *
 * <p>Si lancia con {@code FullProjectStudio.exe --esporta-migrazione[=cartella]}, di solito dal file
 * "Esporta dati per il server.bat". Funziona anche con il gestionale acceso: si collega al database già aperto
 * senza fermarlo. Non avvia il gestionale: esporta, scrive l'esito in {@code esito.txt} e si chiude.</p>
 */
final class EsportazioneMigrazione {

    static final String OPZIONE = "--esporta-migrazione";
    static final String NOME_FILE = "fullprojectstudio-dati.zip";
    private static final DateTimeFormatter DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");

    private EsportazioneMigrazione() {
    }

    /** La cartella richiesta con {@code --esporta-migrazione=...}, o null se l'opzione non c'è. */
    static Path cartellaRichiesta(String[] args, Path cartellaDati) {
        for (String a : args) {
            if (a.equals(OPZIONE)) return cartellaDati.resolve("migrazione");
            if (a.startsWith(OPZIONE + "=")) {
                String dir = a.substring(OPZIONE.length() + 1).trim();
                if (dir.length() > 1 && dir.startsWith("\"") && dir.endsWith("\"")) dir = dir.substring(1, dir.length() - 1);
                return dir.isBlank() ? cartellaDati.resolve("migrazione") : Path.of(dir);
            }
        }
        return null;
    }

    /** Esegue l'esportazione e restituisce il codice di uscita (0 = riuscita). */
    static int esegui(Path cartellaDati, Path destinazione) {
        Path esito = destinazione.resolve("esito.txt");
        try {
            Files.createDirectories(destinazione);
            Path database = cartellaDati.resolve("db").resolve("fullprojectstudio");
            if (!Files.isRegularFile(Path.of(database + ".mv.db"))) {
                throw new IllegalStateException("Non trovo il database del gestionale in " + database + ".mv.db");
            }
            Path finale = destinazione.resolve(NOME_FILE);
            Path temporaneo = destinazione.resolve(NOME_FILE + ".tmp");
            Files.deleteIfExists(temporaneo);

            Map<String, Long> righe;
            // AUTO_SERVER: se il gestionale è acceso ci si collega al suo database senza fermarlo.
            String url = "jdbc:h2:file:" + database.toAbsolutePath().toString().replace('\\', '/') + ";AUTO_SERVER=TRUE;IFEXISTS=TRUE";
            try (Connection c = DriverManager.getConnection(url, "sa", "")) {
                righe = conteggi(c);
                try (Statement st = c.createStatement()) {
                    st.execute("BACKUP TO '" + temporaneo.toAbsolutePath().toString().replace('\\', '/').replace("'", "''") + "'");
                }
            }
            // Solo a copia completa si sostituisce il file precedente: un errore a metà non lascia file rovinati.
            Files.move(temporaneo, finale, StandardCopyOption.REPLACE_EXISTING);
            scrivi(esito, testoRiuscito(finale, righe));
            return 0;
        } catch (Exception e) {
            try {
                Files.createDirectories(destinazione);
                scrivi(esito, "ESPORTAZIONE NON RIUSCITA (" + LocalDateTime.now().format(DATA_ORA) + ")\r\n\r\n" + e.getMessage() + "\r\n");
            } catch (IOException ignorata) {
                // non c'è altro posto dove scrivere l'errore
            }
            return 1;
        }
    }

    private static Map<String, Long> conteggi(Connection c) throws SQLException {
        Map<String, Long> righe = new LinkedHashMap<>();
        String[][] tabelle = {{"studenti", "Studenti"}, {"corsi", "Corsi"}, {"iscrizioni", "Iscrizioni"},
                {"pagamenti", "Pagamenti"}, {"presenze", "Presenze"}, {"istruttori", "Istruttori"},
                {"sale", "Sale"}, {"tipi_abbonamento", "Abbonamenti"}, {"stagioni", "Stagioni"}, {"utenti", "Utenti"}};
        for (String[] t : tabelle) {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t[0])) {
                rs.next();
                righe.put(t[1], rs.getLong(1));
            } catch (SQLException e) {
                // tabella assente in un database molto vecchio: non blocca l'esportazione
            }
        }
        return righe;
    }

    private static String testoRiuscito(Path file, Map<String, Long> righe) {
        StringBuilder sb = new StringBuilder();
        sb.append("ESPORTAZIONE COMPLETATA il ").append(LocalDateTime.now().format(DATA_ORA)).append("\r\n\r\n");
        sb.append("File: ").append(file.toAbsolutePath()).append("\r\n\r\n");
        sb.append("Contenuto:\r\n");
        righe.forEach((nome, n) -> sb.append(String.format("  %-12s %d%n", nome, n).replace("\n", "\r\n")));
        sb.append("\r\nPer portare questi dati sul server segui INSTALLAZIONE-SERVER.md, punto 6-bis:\r\n");
        sb.append("copia il file ").append(NOME_FILE).append(" sul server e lancia il trasferimento.\r\n");
        sb.append("Ogni nuova esportazione sostituisce questo file con i dati aggiornati.\r\n");
        return sb.toString();
    }

    private static void scrivi(Path file, String testo) throws IOException {
        Files.writeString(file, testo, StandardCharsets.UTF_8);
    }
}
