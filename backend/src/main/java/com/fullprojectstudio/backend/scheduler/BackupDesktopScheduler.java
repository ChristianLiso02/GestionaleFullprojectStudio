package com.fullprojectstudio.backend.scheduler;

import com.fullprojectstudio.backend.service.BackupDatabaseService;
import com.fullprojectstudio.backend.service.BackupExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Backup per l'installazione su PC. Il PC della scuola di notte di solito è spento, quindi oltre all'orario
 * fisso (che vale se il PC è acceso) ogni ora controlla se l'ultima copia è troppo vecchia e, se serve,
 * la fa subito: all'accensione del PC il primo controllo scatta dopo pochi minuti.
 */
@Component
@Profile("desktop")
@RequiredArgsConstructor
@Slf4j
public class BackupDesktopScheduler {

    private final BackupDatabaseService backupDatabaseService;
    private final BackupExcelService backupExcelService;

    @Value("${app.backup.ore-massime-tra-backup:20}")
    private int oreMassimeTraBackup;

    /** Orario fisso (default 03:00): copia del database. L'Excel notturno lo genera già BackupScheduler. */
    @Scheduled(cron = "${app.backup.database-cron:0 0 3 * * *}")
    public void backupNotturno() {
        eseguiDatabase();
    }

    /** Recupero dopo un PC rimasto spento: prima verifica 3 minuti dopo l'avvio, poi ogni ora. */
    @Scheduled(initialDelay = 3 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    public void recuperaSeMancante() {
        boolean serve = BackupDatabaseService.serveNuovaCopia(
                backupDatabaseService.ultimaCopia(), Instant.now(), Duration.ofHours(oreMassimeTraBackup));
        if (!serve) {
            return;
        }
        log.info("Nessuna copia recente (oltre {} ore): la creo adesso", oreMassimeTraBackup);
        eseguiDatabase();
        try {
            backupExcelService.generaBackup();
        } catch (Exception e) {
            log.error("Backup Excel di recupero fallito", e);
        }
    }

    private void eseguiDatabase() {
        try {
            backupDatabaseService.backup(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Copia del database fallita", e);
        }
    }
}
