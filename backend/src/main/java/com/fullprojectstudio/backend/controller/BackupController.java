package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.BackupResponse;
import com.fullprojectstudio.backend.service.BackupExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupExcelService backupExcelService;

    /** Genera subito il backup Excel (oltre a quello automatico notturno). */
    @PostMapping("/genera")
    public BackupResponse genera() {
        Path file = backupExcelService.generaBackup();
        return BackupResponse.builder()
                .percorsoFile(file.toAbsolutePath().toString())
                .messaggio("Backup generato con successo.")
                .build();
    }
}
