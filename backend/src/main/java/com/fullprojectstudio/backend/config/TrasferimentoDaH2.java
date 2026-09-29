package com.fullprojectstudio.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Trasferisce tutti i dati del gestionale installato su PC (database H2) nel database del server (PostgreSQL),
 * così passare dal PC al server non fa perdere niente: anagrafiche, iscrizioni, pagamenti, presenze,
 * utenti con le loro password.
 *
 * <p>Si usa una volta sola, sul server, con il database ancora vuoto:
 * <pre>java -jar backend.jar --spring.profiles.active=postgres --trasferisci-da-h2=/percorso/fullprojectstudio.mv.db</pre>
 * Le tabelle del server le crea l'applicazione all'avvio (sono le stesse del PC). Poi il programma copia le righe
 * tabella per tabella rispettando i collegamenti, riallinea i contatori degli id e si chiude.
 * Si rifiuta di partire se nel server ci sono già dati, per non mescolarli.</p>
 *
 * <p>Gira prima di tutti gli altri avvii (utenti e stagione iniziali), che così trovano i dati già presenti.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class TrasferimentoDaH2 implements ApplicationRunner {

    static final String OPZIONE = "trasferisci-da-h2";

    private final DataSource destinazione;
    private final ConfigurableApplicationContext context;

    public TrasferimentoDaH2(DataSource destinazione, ConfigurableApplicationContext context) {
        this.destinazione = destinazione;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption(OPZIONE)) {
            return;
        }
        String file = args.getOptionValues(OPZIONE).get(0);
        int esito = 0;
        try {
            Map<String, Integer> copiate = trasferisci(urlH2(file));
            log.info("Trasferimento completato: {}", copiate);
            System.out.println("\nTRASFERIMENTO COMPLETATO. Righe copiate per tabella: " + copiate + "\n");
        } catch (Exception e) {
            log.error("Trasferimento non riuscito", e);
            System.out.println("\nTRASFERIMENTO NON RIUSCITO: " + e.getMessage() + "\n");
            esito = 1;
        }
        // È un'operazione una tantum: finito il trasferimento il programma si chiude.
        int codice = esito;
        System.exit(SpringApplication.exit(context, () -> codice));
    }

    /** Accetta sia il file "fullprojectstudio.mv.db" sia il percorso senza estensione. */
    static String urlH2(String file) {
        String percorso = file.replace('\\', '/');
        if (percorso.endsWith(".mv.db")) {
            percorso = percorso.substring(0, percorso.length() - ".mv.db".length());
        }
        // IFEXISTS: se il file non c'è H2 non deve crearne uno vuoto (e "trasferire" zero righe)
        return "jdbc:h2:file:" + percorso + ";IFEXISTS=TRUE;ACCESS_MODE_DATA=r";
    }

    Map<String, Integer> trasferisci(String urlOrigine) throws SQLException {
        try (Connection origine = DriverManager.getConnection(urlOrigine, "sa", "");
             Connection dest = destinazione.getConnection()) {

            Map<String, String> tabelleOrigine = tabelle(origine);   // nome minuscolo -> nome reale
            Map<String, String> tabelleDest = tabelle(dest);
            List<String> daCopiare = new ArrayList<>(tabelleOrigine.keySet());
            daCopiare.retainAll(tabelleDest.keySet());
            if (daCopiare.isEmpty()) {
                throw new IllegalStateException("Nessuna tabella in comune: il file indicato non sembra un database del gestionale.");
            }
            for (String t : daCopiare) {
                if (conta(dest, tabelleDest.get(t)) > 0) {
                    throw new IllegalStateException("Nel database del server ci sono già dati (tabella " + t
                            + "): il trasferimento va fatto su un database appena creato.");
                }
            }

            List<String> ordine = ordinePerCollegamenti(dest, daCopiare, tabelleDest);
            Map<String, Integer> copiate = new LinkedHashMap<>();
            dest.setAutoCommit(false);
            try {
                for (String t : ordine) {
                    copiate.put(t, copiaTabella(origine, tabelleOrigine.get(t), dest, tabelleDest.get(t)));
                }
                riallineaContatori(dest, ordine, tabelleDest);
                dest.commit();
            } catch (SQLException | RuntimeException e) {
                dest.rollback();
                throw e;
            }
            return copiate;
        }
    }

    private Map<String, String> tabelle(Connection c) throws SQLException {
        Map<String, String> nomi = new TreeMap<>();
        String schema = c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("h2") ? "PUBLIC" : "public";
        try (ResultSet rs = c.getMetaData().getTables(null, schema, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String nome = rs.getString("TABLE_NAME");
                nomi.put(nome.toLowerCase(Locale.ROOT), nome);
            }
        }
        return nomi;
    }

    private long conta(Connection c, String tabella) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabella)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Prima le tabelle a cui altre fanno riferimento (es. studenti prima di iscrizioni, iscrizioni prima di pagamenti). */
    private List<String> ordinePerCollegamenti(Connection dest, List<String> tabelle, Map<String, String> nomiReali) throws SQLException {
        DatabaseMetaData meta = dest.getMetaData();
        Map<String, Set<String>> dipendeDa = new HashMap<>();
        for (String t : tabelle) {
            Set<String> padri = new LinkedHashSet<>();
            try (ResultSet rs = meta.getImportedKeys(null, null, nomiReali.get(t))) {
                while (rs.next()) {
                    String padre = rs.getString("PKTABLE_NAME").toLowerCase(Locale.ROOT);
                    if (!padre.equals(t) && tabelle.contains(padre)) padri.add(padre);
                }
            }
            dipendeDa.put(t, padri);
        }
        List<String> ordine = new ArrayList<>();
        Set<String> inCorso = new LinkedHashSet<>();
        for (String t : tabelle) visita(t, dipendeDa, ordine, inCorso);
        return ordine;
    }

    private void visita(String t, Map<String, Set<String>> dipendeDa, List<String> ordine, Set<String> inCorso) {
        if (ordine.contains(t) || !inCorso.add(t)) return;
        for (String padre : dipendeDa.getOrDefault(t, Set.of())) visita(padre, dipendeDa, ordine, inCorso);
        ordine.add(t);
    }

    private int copiaTabella(Connection origine, String tabOrigine, Connection dest, String tabDest) throws SQLException {
        // Colonne della destinazione (nome minuscolo -> tipo SQL); si copiano quelle presenti in entrambe.
        Map<String, Integer> tipiDest = new LinkedHashMap<>();
        Map<String, String> nomiDest = new HashMap<>();
        try (ResultSet rs = dest.getMetaData().getColumns(null, null, tabDest, "%")) {
            while (rs.next()) {
                String nome = rs.getString("COLUMN_NAME");
                tipiDest.put(nome.toLowerCase(Locale.ROOT), rs.getInt("DATA_TYPE"));
                nomiDest.put(nome.toLowerCase(Locale.ROOT), nome);
            }
        }
        int righe = 0;
        try (Statement st = origine.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + tabOrigine)) {
            List<String> colonne = new ArrayList<>();
            List<Integer> indiciOrigine = new ArrayList<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String nome = rs.getMetaData().getColumnName(i).toLowerCase(Locale.ROOT);
                if (tipiDest.containsKey(nome)) {
                    colonne.add(nome);
                    indiciOrigine.add(i);
                }
            }
            String sql = "INSERT INTO " + tabDest + " (" + String.join(", ", colonne.stream().map(nomiDest::get).toList())
                    + ") VALUES (" + String.join(", ", colonne.stream().map(c -> "?").toList()) + ")";
            try (PreparedStatement ins = dest.prepareStatement(sql)) {
                while (rs.next()) {
                    for (int k = 0; k < colonne.size(); k++) {
                        Object valore = rs.getObject(indiciOrigine.get(k));
                        int tipo = tipiDest.get(colonne.get(k));
                        if (valore == null) {
                            ins.setNull(k + 1, tipo);
                        } else if (tipo == Types.VARCHAR || tipo == Types.CHAR || tipo == Types.LONGVARCHAR) {
                            // Le colonne "enum" di H2 diventano testo sul server
                            ins.setString(k + 1, valore.toString());
                        } else {
                            ins.setObject(k + 1, valore);
                        }
                    }
                    ins.addBatch();
                    righe++;
                }
                ins.executeBatch();
            }
        }
        log.info("Tabella {}: {} righe copiate", tabDest, righe);
        return righe;
    }

    private boolean haColonnaId(Connection c, String tabella) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, null, tabella, "id")) {
            return rs.next();
        }
    }

    /** Dopo aver inserito righe con id già decisi, il prossimo id nuovo deve partire dal massimo esistente. */
    private void riallineaContatori(Connection dest, List<String> tabelle, Map<String, String> nomiReali) throws SQLException {
        if (!dest.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres")) return;
        for (String t : tabelle) {
            String nome = nomiReali.get(t);
            if (!haColonnaId(dest, nome)) continue; // tabelle di collegamento (es. corso_istruttori)
            String sequenza;
            try (PreparedStatement ps = dest.prepareStatement("SELECT pg_get_serial_sequence(?, 'id')")) {
                ps.setString(1, nome);
                try (ResultSet rs = ps.executeQuery()) {
                    sequenza = rs.next() ? rs.getString(1) : null;
                }
            }
            if (sequenza == null) continue;
            try (Statement st = dest.createStatement()) {
                st.execute("SELECT setval('" + sequenza + "', COALESCE((SELECT MAX(id) FROM " + nome + "), 0) + 1, false)");
            }
        }
    }
}
