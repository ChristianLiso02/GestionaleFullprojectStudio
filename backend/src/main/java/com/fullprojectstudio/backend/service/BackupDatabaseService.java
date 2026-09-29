package com.fullprojectstudio.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Copia completa del database H2 (installazione su PC): un file .zip con tutti i dati, da cui si può
 * ricostruire il gestionale. Ne tiene le ultime N e cancella le più vecchie.
 */
@Service
@Profile("desktop")
@Slf4j
public class BackupDatabaseService {

    private static final String PREFISSO = "database-";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    private final JdbcTemplate jdbcTemplate;
    private final Path cartella;
    private final int copieDaTenere;

    public BackupDatabaseService(JdbcTemplate jdbcTemplate,
                                 @Value("${app.backup.dir}") String backupDir,
                                 @Value("${app.backup.copie-database-da-tenere:30}") int copieDaTenere) {
        this.jdbcTemplate = jdbcTemplate;
        this.cartella = Path.of(backupDir, "database");
        this.copieDaTenere = copieDaTenere;
    }

    /** Crea subito una copia del database e ripulisce quelle troppo vecchie. */
    public Path backup(LocalDateTime adesso) {
        try {
            Files.createDirectories(cartella);
            Path file = cartella.resolve(PREFISSO + adesso.format(FORMATO) + ".zip");
            Files.deleteIfExists(file);
            // BACKUP TO è un comando di H2: copia coerente del database mentre è in uso.
            jdbcTemplate.execute("BACKUP TO '" + file.toAbsolutePath().toString().replace('\\', '/').replace("'", "''") + "'");
            log.info("Copia del database creata: {}", file.toAbsolutePath());
            elimina(daEliminare(elencoCopie(), copieDaTenere));
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile creare la copia del database: " + e.getMessage(), e);
        }
    }

    /** Quando è stata fatta l'ultima copia, se ce n'è una. */
    public Optional<Instant> ultimaCopia() {
        return elencoCopie().stream()
                .map(p -> p.toFile().lastModified())
                .max(Long::compare)
                .map(Instant::ofEpochMilli);
    }

    /** Serve una nuova copia se non ce n'è nessuna oppure l'ultima è più vecchia di maxTra. */
    public static boolean serveNuovaCopia(Optional<Instant> ultima, Instant ora, Duration maxTra) {
        return ultima.isEmpty() || Duration.between(ultima.get(), ora).compareTo(maxTra) > 0;
    }

    /** Le copie più vecchie da cancellare per tenerne al massimo {@code tieni} (i nomi contengono la data). */
    public static List<Path> daEliminare(List<Path> copie, int tieni) {
        List<Path> dalPiuRecente = copie.stream()
                .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                .toList();
        return dalPiuRecente.size() <= Math.max(1, tieni) ? List.of() : dalPiuRecente.subList(Math.max(1, tieni), dalPiuRecente.size());
    }

    private List<Path> elencoCopie() {
        if (!Files.isDirectory(cartella)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(cartella)) {
            return files.filter(p -> p.getFileName().toString().startsWith(PREFISSO) && p.getFileName().toString().endsWith(".zip")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void elimina(List<Path> copie) throws IOException {
        for (Path p : copie) {
            Files.deleteIfExists(p);
            log.info("Copia del database eliminata (troppo vecchia): {}", p.getFileName());
        }
    }
}
