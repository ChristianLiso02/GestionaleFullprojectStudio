package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.ArchivioBackupDto;
import com.fullprojectstudio.backend.dto.BackupResponse;
import com.fullprojectstudio.backend.service.ArchivioBackupService;
import com.fullprojectstudio.backend.service.BackupExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupExcelService backupExcelService;
    private final ArchivioBackupService archivioBackupService;

    /** Genera subito il backup Excel (oltre a quello automatico notturno). */
    @PostMapping("/genera")
    public BackupResponse genera() {
        Path file = backupExcelService.generaBackup();
        return BackupResponse.builder()
                .percorsoFile(file.toAbsolutePath().toString())
                .messaggio("Backup generato con successo.")
                .build();
    }

    /** Backup presenti (Excel e copie complete del database), dal più recente. */
    @GetMapping
    public ArchivioBackupDto elenco() {
        return archivioBackupService.elenco();
    }

    @GetMapping("/file/{nome}")
    public ResponseEntity<Resource> scarica(@PathVariable String nome) {
        Path file = archivioBackupService.file(nome);
        MediaType tipo = nome.endsWith(".zip") ? MediaType.parseMediaType("application/zip")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nome).build().toString())
                .contentType(tipo)
                .body(new FileSystemResource(file));
    }
}
