package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.service.BackupDatabaseService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Il PC della scuola di notte di solito è spento: la copia del database si rifà all'accensione se l'ultima
 * è troppo vecchia, e si tengono solo le ultime N.
 */
class BackupDatabaseServiceTest {

    private static final Instant ORA = Instant.parse("2026-10-05T08:00:00Z");
    private static final Duration VENTI_ORE = Duration.ofHours(20);

    @Test
    void senzaNessunaCopiaSiFaSubito() {
        assertThat(BackupDatabaseService.serveNuovaCopia(Optional.empty(), ORA, VENTI_ORE)).isTrue();
    }

    @Test
    void unaCopiaDiIeriSeraNonBastaLaMattinaDopo() {
        // Ultima copia ieri alle 17:00, oggi sono le 08:00: sono passate 15 ore, ancora recente
        assertThat(BackupDatabaseService.serveNuovaCopia(Optional.of(ORA.minus(Duration.ofHours(15))), ORA, VENTI_ORE)).isFalse();
        // Dopo un fine settimana con il PC spento, è passato molto di più
        assertThat(BackupDatabaseService.serveNuovaCopia(Optional.of(ORA.minus(Duration.ofHours(63))), ORA, VENTI_ORE)).isTrue();
    }

    @Test
    void siTengonoSoloLeUltimeCopie() {
        List<Path> copie = List.of(
                Path.of("database-2026-10-01-0300.zip"),
                Path.of("database-2026-10-04-0300.zip"),
                Path.of("database-2026-10-02-0300.zip"),
                Path.of("database-2026-10-03-0300.zip"));

        assertThat(BackupDatabaseService.daEliminare(copie, 2))
                .containsExactly(Path.of("database-2026-10-02-0300.zip"), Path.of("database-2026-10-01-0300.zip"));
        assertThat(BackupDatabaseService.daEliminare(copie, 10)).isEmpty();
    }

    @Test
    void l_ultimaCopiaNonSiCancellaMai() {
        List<Path> copie = List.of(Path.of("database-2026-10-01-0300.zip"), Path.of("database-2026-10-02-0300.zip"));

        assertThat(BackupDatabaseService.daEliminare(copie, 0)).containsExactly(Path.of("database-2026-10-01-0300.zip"));
    }
}
