package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.config.EsportatoreSql;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** L'export SQL è in dialetto PostgreSQL, rispetta l'ordine dei collegamenti e non importa su un server già in uso. */
class EsportatoreSqlTest {

    @TempDir Path cartella;

    private String esporta(String... comandi) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "")) {
            try (Statement st = c.createStatement()) {
                for (String sql : comandi) st.execute(sql);
            }
            Path file = cartella.resolve("dati.sql");
            Map<String, Long> righe = EsportatoreSql.esporta(c, file, "01/01/2026 alle 10:00");
            assertThat(righe).isNotEmpty();
            return Files.readString(file);
        }
    }

    private static final String[] SCHEMA = {
            "CREATE TABLE utenti (id BIGINT PRIMARY KEY, username VARCHAR(50))",
            "CREATE TABLE stagioni (id BIGINT PRIMARY KEY, nome VARCHAR(50))",
            "CREATE TABLE iscrizioni (id BIGINT PRIMARY KEY, studente_id BIGINT NOT NULL, data_iscrizione DATE)",
            "CREATE TABLE studenti (id BIGINT PRIMARY KEY, nome VARCHAR(50), attivo BOOLEAN, importo DECIMAL(10,2), note VARCHAR(200))",
            "ALTER TABLE iscrizioni ADD FOREIGN KEY (studente_id) REFERENCES studenti(id)",
            "CREATE TABLE corso_giorni (corso_id BIGINT NOT NULL, giorno VARCHAR(20))"
    };

    @Test
    void scriveInsertInDialettoPostgresConIValoriGiusti() throws Exception {
        String sql = esporta(concat(SCHEMA,
                "INSERT INTO studenti VALUES (1, 'Mario D''Angelo', TRUE, 60.50, NULL)",
                "INSERT INTO iscrizioni VALUES (1, 1, DATE '2026-09-01')"));

        assertThat(sql).contains("INSERT INTO studenti (id, nome, attivo, importo, note) VALUES (1, 'Mario D''Angelo', TRUE, 60.50, NULL);");
        assertThat(sql).contains("INSERT INTO iscrizioni (id, studente_id, data_iscrizione) VALUES (1, 1, '2026-09-01');");
        assertThat(sql).startsWith("-- FullProject Studio").contains("BEGIN;").endsWith("COMMIT;\n");
    }

    @Test
    void ISociVengonoPrimaDiChiLiUsa() throws Exception {
        String sql = esporta(concat(SCHEMA, "INSERT INTO studenti VALUES (1, 'A', TRUE, 1, NULL)", "INSERT INTO iscrizioni VALUES (1, 1, NULL)"));

        assertThat(sql.indexOf("INSERT INTO studenti")).isPositive().isLessThan(sql.indexOf("INSERT INTO iscrizioni"));
    }

    @Test
    void ilControlloFermaLImportazioneSeIlServerHaGiaDatiMaNonConsideraUtentiEStagioni() throws Exception {
        String sql = esporta(SCHEMA);
        String controllo = sql.substring(sql.indexOf("DO $$"), sql.indexOf("END $$"));

        assertThat(controllo).contains("EXISTS (SELECT 1 FROM studenti)", "EXISTS (SELECT 1 FROM iscrizioni)", "RAISE EXCEPTION")
                .doesNotContain("FROM utenti", "FROM stagioni");
        assertThat(sql).contains("DELETE FROM utenti;", "DELETE FROM stagioni;");
    }

    @Test
    void ContatoriRiallineatiSoloPerLeTabelleConId() throws Exception {
        String sql = esporta(SCHEMA);

        assertThat(sql).contains("pg_get_serial_sequence('studenti', 'id')", "pg_get_serial_sequence('iscrizioni', 'id')");
        assertThat(sql).doesNotContain("pg_get_serial_sequence('corso_giorni'");
    }

    @Test
    void valoriLetterali() {
        assertThat(EsportatoreSql.letterale(null)).isEqualTo("NULL");
        assertThat(EsportatoreSql.letterale(false)).isEqualTo("FALSE");
        assertThat(EsportatoreSql.letterale(new BigDecimal("1E+2"))).isEqualTo("100");
        assertThat(EsportatoreSql.letterale(42L)).isEqualTo("42");
        assertThat(EsportatoreSql.letterale(LocalDate.of(2026, 9, 1))).isEqualTo("'2026-09-01'");
        assertThat(EsportatoreSql.letterale("l'\\altro")).isEqualTo("'l''\\altro'");
        assertThat(EsportatoreSql.letterale(new byte[]{1, (byte) 0xAB})).isEqualTo("'\\x01ab'");
    }

    private static String[] concat(String[] base, String... altri) {
        String[] r = new String[base.length + altri.length];
        System.arraycopy(base, 0, r, 0, base.length);
        System.arraycopy(altri, 0, r, base.length, altri.length);
        return r;
    }
}
