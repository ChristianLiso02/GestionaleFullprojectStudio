package com.fullprojectstudio.backend.scheduler;

import com.fullprojectstudio.backend.service.BackupExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final BackupExcelService backupExcelService;

    /** Ogni notte (default 02:00, configurabile con BACKUP_CRON) genera il backup Excel. */
    @Scheduled(cron = "${app.backup.cron}")
    public void generaBackupNotturno() {
        log.info("Avvio generazione backup Excel schedulato");
        try {
            backupExcelService.generaBackup();
        } catch (Exception e) {
            log.error("Generazione backup Excel schedulato fallita", e);
        }
    }
}
