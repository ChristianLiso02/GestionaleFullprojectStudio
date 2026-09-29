package com.fullprojectstudio.backend.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Scrive tutti i dati di un database H2 del gestionale in un file SQL con sintassi PostgreSQL: solo comandi
 * INSERT (le tabelle le crea il gestionale al primo avvio sul server), in un'unica transazione, con un controllo
 * che si ferma se il server contiene già dati, e il riallineamento dei contatori degli id alla fine.
 *
 * <p>Si importa con:
 * {@code psql -v ON_ERROR_STOP=1 -U utente -d database -f fullprojectstudio-dati.sql}</p>
 */
public final class EsportatoreSql {

    /** Tabelle che il gestionale riempie da solo al primo avvio: sul server vanno sostituite con quelle esportate. */
    private static final Set<String> INIZIALI = Set.of("utenti", "stagioni");

    private EsportatoreSql() {
    }

    /** Scrive il file SQL e restituisce quante righe ha esportato per ogni tabella. */
    public static Map<String, Long> esporta(Connection origine, Path file, String dataOra) throws SQLException, IOException {
        Map<String, String> nomi = tabelle(origine);
        List<String> ordine = ordine(origine, nomi);
        Map<String, Long> righe = new java.util.LinkedHashMap<>();

        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("-- FullProject Studio: esportazione dei dati del " + dataOra + "\n");
            w.write("-- Formato PostgreSQL, solo INSERT: le tabelle le crea il gestionale al primo avvio sul server.\n");
            w.write("-- Contiene dati personali e sanitari (note mediche) e le password degli utenti in forma cifrata:\n");
            w.write("-- va conservato e condiviso con attenzione.\n");
            w.write("--\n");
            w.write("-- Importazione (dopo aver avviato il gestionale sul server almeno una volta):\n");
            w.write("--   psql -v ON_ERROR_STOP=1 -U <utente> -d <database> -f fullprojectstudio-dati.sql\n");
            w.write("-- Se il database del server contiene già dei dati, l'importazione si ferma senza modificare niente.\n\n");
            w.write("BEGIN;\n\n");

            List<String> daControllare = new ArrayList<>();
            for (String t : ordine) if (!INIZIALI.contains(t)) daControllare.add(t);
            if (!daControllare.isEmpty()) {
                w.write("DO $$\nBEGIN\n  IF ");
                for (int i = 0; i < daControllare.size(); i++) {
                    if (i > 0) w.write("\n     OR ");
                    w.write("EXISTS (SELECT 1 FROM " + daControllare.get(i) + ")");
                }
                w.write(" THEN\n    RAISE EXCEPTION 'Il database del server contiene già dei dati: importazione annullata.';\n  END IF;\nEND $$;\n\n");
            }
            w.write("-- Utenti e stagione creati dal gestionale al primo avvio: vengono sostituiti da quelli esportati.\n");
            w.write("DELETE FROM utenti;\nDELETE FROM stagioni;\n\n");

            for (String t : ordine) {
                righe.put(t, scriviTabella(origine, nomi.get(t), t, w));
            }

            w.write("\n-- I prossimi id nuovi ripartono dopo l'ultimo importato.\n");
            for (String t : ordine) {
                if (haColonnaId(origine, nomi.get(t))) {
                    w.write("SELECT setval(pg_get_serial_sequence('" + t + "', 'id'), COALESCE((SELECT MAX(id) FROM " + t
                            + "), 0) + 1, false);\n");
                }
            }
            w.write("\nCOMMIT;\n");
        }
        return righe;
    }

    private static long scriviTabella(Connection c, String nomeReale, String nome, BufferedWriter w) throws SQLException, IOException {
        long n = 0;
        String ordina = haColonnaId(c, nomeReale) ? " ORDER BY id" : "";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + nomeReale + ordina)) {
            ResultSetMetaData md = rs.getMetaData();
            StringBuilder colonne = new StringBuilder();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if (i > 1) colonne.append(", ");
                colonne.append(md.getColumnName(i).toLowerCase(Locale.ROOT));
            }
            while (rs.next()) {
                StringBuilder valori = new StringBuilder();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (i > 1) valori.append(", ");
                    valori.append(letterale(rs.getObject(i)));
                }
                w.write("INSERT INTO " + nome + " (" + colonne + ") VALUES (" + valori + ");\n");
                n++;
            }
        }
        return n;
    }

    /** Il valore come lo scrive PostgreSQL in un comando INSERT. */
    public static String letterale(Object valore) {
        if (valore == null) return "NULL";
        if (valore instanceof Boolean b) return b ? "TRUE" : "FALSE";
        if (valore instanceof BigDecimal d) return d.toPlainString();
        if (valore instanceof Number n) return n.toString();
        if (valore instanceof byte[] bytes) {
            StringBuilder sb = new StringBuilder("'\\x");
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.append("'").toString();
        }
        // Testi, enum, date e orari (LocalDate, LocalDateTime... in formato ISO, che PostgreSQL legge direttamente)
        return "'" + valore.toString().replace("'", "''") + "'";
    }

    private static Map<String, String> tabelle(Connection c) throws SQLException {
        Map<String, String> nomi = new TreeMap<>();
        try (ResultSet rs = c.getMetaData().getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String nome = rs.getString("TABLE_NAME");
                nomi.put(nome.toLowerCase(Locale.ROOT), nome);
            }
        }
        return nomi;
    }

    /** Prima le tabelle a cui altre fanno riferimento (studenti prima delle iscrizioni, iscrizioni prima dei pagamenti). */
    private static List<String> ordine(Connection c, Map<String, String> nomi) throws SQLException {
        DatabaseMetaData meta = c.getMetaData();
        Map<String, Set<String>> dipendeDa = new HashMap<>();
        for (String t : nomi.keySet()) {
            Set<String> padri = new LinkedHashSet<>();
            try (ResultSet rs = meta.getImportedKeys(null, "PUBLIC", nomi.get(t))) {
                while (rs.next()) {
                    String padre = rs.getString("PKTABLE_NAME").toLowerCase(Locale.ROOT);
                    if (!padre.equals(t) && nomi.containsKey(padre)) padri.add(padre);
                }
            }
            dipendeDa.put(t, padri);
        }
        List<String> risultato = new ArrayList<>();
        Set<String> inCorso = new LinkedHashSet<>();
        for (String t : nomi.keySet()) visita(t, dipendeDa, risultato, inCorso);
        return risultato;
    }

    private static void visita(String t, Map<String, Set<String>> dipendeDa, List<String> risultato, Set<String> inCorso) {
        if (risultato.contains(t) || !inCorso.add(t)) return;
        for (String padre : dipendeDa.getOrDefault(t, Set.of())) visita(padre, dipendeDa, risultato, inCorso);
        risultato.add(t);
    }

    private static boolean haColonnaId(Connection c, String tabella) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, "PUBLIC", tabella, "ID")) {
            return rs.next();
        }
    }
}
