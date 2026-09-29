package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.ArchivioBackupDto;
import com.fullprojectstudio.backend.dto.FileBackupDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Elenca i backup presenti nella cartella dei backup e li rende scaricabili dal gestionale: sul server è
 * l'unico modo per la segreteria di averne una copia. Si possono scaricare solo i file con i nomi
 * creati dal gestionale, niente altro dalla cartella.
 */
@Service
public class ArchivioBackupService {

    private static final Pattern EXCEL = Pattern.compile("backup-\\d{4}-\\d{2}-\\d{2}\\.xlsx");
    private static final Pattern DATABASE = Pattern.compile("database-\\d{4}-\\d{2}-\\d{2}-\\d{4}\\.zip");
    private static final int MAX_PER_TIPO = 30;

    private final Path cartella;
    private final String cron;

    public ArchivioBackupService(@Value("${app.backup.dir}") String backupDir, @Value("${app.backup.cron}") String cron) {
        this.cartella = Path.of(backupDir);
        this.cron = cron;
    }

    public ArchivioBackupDto elenco() {
        List<FileBackupDto> file = new ArrayList<>();
        file.addAll(elenca(cartella, EXCEL, "EXCEL"));
        file.addAll(elenca(cartella.resolve("database"), DATABASE, "DATABASE"));
        file.sort(Comparator.comparing(FileBackupDto::getData).reversed());
        return ArchivioBackupDto.builder()
                .cartella(cartella.toAbsolutePath().normalize().toString())
                .orarioAutomatico(orario(cron))
                .file(file)
                .build();
    }

    /** Il file richiesto, solo se ha un nome da backup ed esiste davvero nella cartella dei backup. */
    public Path file(String nome) {
        Path percorso;
        if (EXCEL.matcher(nome).matches()) {
            percorso = cartella.resolve(nome);
        } else if (DATABASE.matcher(nome).matches()) {
            percorso = cartella.resolve("database").resolve(nome);
        } else {
            throw new ResourceNotFoundException("Backup non trovato: " + nome);
        }
        if (!Files.isRegularFile(percorso)) {
            throw new ResourceNotFoundException("Backup non trovato: " + nome);
        }
        return percorso;
    }

    private List<FileBackupDto> elenca(Path dir, Pattern nomi, String tipo) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> nomi.matcher(p.getFileName().toString()).matches())
                    .map(p -> dto(p, tipo))
                    .sorted(Comparator.comparing(FileBackupDto::getData).reversed())
                    .limit(MAX_PER_TIPO)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private FileBackupDto dto(Path p, String tipo) {
        try {
            return FileBackupDto.builder()
                    .tipo(tipo)
                    .nome(p.getFileName().toString())
                    .data(LocalDateTime.ofInstant(Instant.ofEpochMilli(Files.getLastModifiedTime(p).toMillis()), ZoneId.systemDefault()))
                    .dimensione(Files.size(p))
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** "0 0 2 * * *" -> "02:00"; per espressioni più complesse restituisce l'espressione così com'è. */
    static String orario(String cron) {
        String[] parti = cron.trim().split("\\s+");
        if (parti.length == 6 && parti[1].matches("\\d{1,2}") && parti[2].matches("\\d{1,2}") && parti[3].equals("*")) {
            return String.format("%02d:%02d", Integer.parseInt(parti[2]), Integer.parseInt(parti[1]));
        }
        return cron;
    }
}
