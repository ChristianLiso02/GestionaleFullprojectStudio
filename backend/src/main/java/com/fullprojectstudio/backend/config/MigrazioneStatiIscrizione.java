package com.fullprojectstudio.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Porta i database esistenti ai nuovi stati delle iscrizioni (ATTIVA / RITIRATO).
 * Con ddl-auto=update Hibernate non modifica le colonne esistenti: su H2 "stato" è un ENUM
 * con i vecchi valori, su PostgreSQL ha un vincolo CHECK con i vecchi valori. Entrambi
 * rifiuterebbero RITIRATO, quindi qui si rende la colonna un testo libero e si convertono
 * le vecchie iscrizioni SCADUTA/ANNULLATA in RITIRATO. Idempotente: gira ad ogni avvio.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class MigrazioneStatiIscrizione implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        String database = jdbc.execute((ConnectionCallback<String>) c -> c.getMetaData().getDatabaseProductName());

        if ("H2".equalsIgnoreCase(database)) {
            List<String> tipo = jdbc.queryForList(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ISCRIZIONI' AND COLUMN_NAME = 'STATO'",
                    String.class);
            if (tipo.contains("ENUM")) {
                jdbc.execute("ALTER TABLE iscrizioni ALTER COLUMN stato SET DATA TYPE VARCHAR(20)");
            }
        } else if ("PostgreSQL".equalsIgnoreCase(database)) {
            jdbc.execute("ALTER TABLE iscrizioni DROP CONSTRAINT IF EXISTS iscrizioni_stato_check");
        }

        int convertite = jdbc.update("UPDATE iscrizioni SET stato = 'RITIRATO' WHERE stato IN ('SCADUTA', 'ANNULLATA')");
        if (convertite > 0) {
            log.info("Migrazione stati iscrizione: {} iscrizioni scadute/annullate convertite in RITIRATO", convertite);
        }
    }
}
