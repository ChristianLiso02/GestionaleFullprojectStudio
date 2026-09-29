package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.security.LimiteTentativiLogin;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dopo 5 password sbagliate in 15 minuti il login si blocca; si sblocca da solo quando
 * il tentativo più vecchio esce dalla finestra, oppure subito dopo un login riuscito.
 */
class LimiteTentativiLoginTest {

    private static final Instant T0 = Instant.parse("2026-10-01T10:00:00Z");
    private static final String CHIAVE = "1.2.3.4|segreteria";

    private final LimiteTentativiLogin limite = new LimiteTentativiLogin(5, 15);

    private void sbaglia(int volte, Instant da) {
        for (int i = 0; i < volte; i++) limite.registraFallimento(CHIAVE, da.plusSeconds(i));
    }

    @Test
    void quattroErroriNonBloccano() {
        sbaglia(4, T0);
        assertThat(limite.minutiDiAttesa(CHIAVE, T0.plusSeconds(10))).isZero();
    }

    @Test
    void alQuintoErroreIlLoginSiBloccaPerUnQuartoDora() {
        sbaglia(5, T0);
        assertThat(limite.minutiDiAttesa(CHIAVE, T0.plusSeconds(10))).isEqualTo(15);
        assertThat(limite.minutiDiAttesa(CHIAVE, T0.plus(Duration.ofMinutes(10)))).isEqualTo(5);
        assertThat(limite.minutiDiAttesa(CHIAVE, T0.plus(Duration.ofMinutes(15)).plusSeconds(1))).isZero();
    }

    @Test
    void unAltroUtenteOIndirizzoNonEBloccato() {
        sbaglia(5, T0);
        assertThat(limite.minutiDiAttesa("1.2.3.4|admin", T0.plusSeconds(10))).isZero();
        assertThat(limite.minutiDiAttesa("5.6.7.8|segreteria", T0.plusSeconds(10))).isZero();
    }

    @Test
    void unLoginRiuscitoAzzeraGliErrori() {
        sbaglia(4, T0);
        limite.azzera(CHIAVE);
        sbaglia(4, T0.plusSeconds(30));
        assertThat(limite.minutiDiAttesa(CHIAVE, T0.plusSeconds(60))).isZero();
    }
}
