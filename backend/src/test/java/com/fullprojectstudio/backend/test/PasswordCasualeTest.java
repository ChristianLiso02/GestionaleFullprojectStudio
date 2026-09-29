package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.security.PasswordCasuale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordCasualeTest {

    @Test
    void haLaLunghezzaRichiestaESenzaCaratteriAmbigui() {
        for (int i = 0; i < 200; i++) {
            assertThat(PasswordCasuale.genera(12)).hasSize(12).matches("[a-hj-km-np-zA-HJ-NP-Z2-9]+");
        }
    }

    @Test
    void duePasswordNonSonoUguali() {
        assertThat(PasswordCasuale.genera(12)).isNotEqualTo(PasswordCasuale.genera(12));
    }
}
