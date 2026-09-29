package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.ArchivioBackupDto;
import com.fullprojectstudio.backend.dto.FileBackupDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.service.ArchivioBackupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Dalla pagina Backup si vedono e si scaricano solo i backup creati dal gestionale: nessun altro file
 * della cartella (né fuori da essa) è raggiungibile.
 */
class ArchivioBackupServiceTest {

    @TempDir Path cartella;

    private ArchivioBackupService servizio() {
        return new ArchivioBackupService(cartella.toString(), "0 0 2 * * *");
    }

    private void crea(Path file, String data) throws Exception {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "x");
        Files.setLastModifiedTime(file, FileTime.from(Instant.parse(data)));
    }

    @Test
    void elencaExcelECopieCompleteDalPiuRecente() throws Exception {
        crea(cartella.resolve("backup-2026-09-28.xlsx"), "2026-09-28T02:00:00Z");
        crea(cartella.resolve("backup-2026-09-29.xlsx"), "2026-09-29T02:00:00Z");
        crea(cartella.resolve("database/database-2026-09-29-0300.zip"), "2026-09-29T03:00:00Z");
        crea(cartella.resolve("backup-ultimo.xlsx"), "2026-09-29T02:00:00Z");
        crea(cartella.resolve("appunti.txt"), "2026-09-29T02:00:00Z");

        ArchivioBackupDto archivio = servizio().elenco();

        assertThat(archivio.getOrarioAutomatico()).isEqualTo("02:00");
        assertThat(archivio.getFile()).extracting(FileBackupDto::getNome)
                .containsExactly("database-2026-09-29-0300.zip", "backup-2026-09-29.xlsx", "backup-2026-09-28.xlsx");
        assertThat(archivio.getFile()).extracting(FileBackupDto::getTipo).containsExactly("DATABASE", "EXCEL", "EXCEL");
    }

    @Test
    void siScaricanoSoloIBackupEsistenti() throws Exception {
        crea(cartella.resolve("backup-2026-09-29.xlsx"), "2026-09-29T02:00:00Z");
        crea(cartella.resolve("database/database-2026-09-29-0300.zip"), "2026-09-29T03:00:00Z");

        assertThat(servizio().file("backup-2026-09-29.xlsx")).isEqualTo(cartella.resolve("backup-2026-09-29.xlsx"));
        assertThat(servizio().file("database-2026-09-29-0300.zip")).isEqualTo(cartella.resolve("database/database-2026-09-29-0300.zip"));
        assertThatThrownBy(() -> servizio().file("backup-2020-01-01.xlsx")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void nomiDiversiDaUnBackupSonoRifiutati() {
        for (String nome : new String[]{"../application.yml", "..\\\\config.txt", "appunti.txt", "backup-ultimo.xlsx", "database/../../x.zip"}) {
            assertThatThrownBy(() -> servizio().file(nome)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    void senzaCartellaNonCiSonoBackup() {
        ArchivioBackupService s = new ArchivioBackupService(cartella.resolve("non-esiste").toString(), "0 30 3 * * *");
        assertThat(s.elenco().getFile()).isEmpty();
        assertThat(s.elenco().getOrarioAutomatico()).isEqualTo("03:30");
    }
}
