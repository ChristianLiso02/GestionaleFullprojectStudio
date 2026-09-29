package com.fullprojectstudio.backend.test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** Il percorso del database del PC si può indicare con o senza estensione, anche con le "\\" di Windows. */
class TrasferimentoDaH2Test {

    private String url(String file) throws Exception {
        Class<?> c = Class.forName("com.fullprojectstudio.backend.config.TrasferimentoDaH2");
        Method m = c.getDeclaredMethod("urlH2", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, file);
    }

    @Test
    void togliEstensioneEUsaSoloLetturaSenzaCreareFileNuovi() throws Exception {
        assertThat(url("/import/fullprojectstudio.mv.db"))
                .isEqualTo("jdbc:h2:file:/import/fullprojectstudio;IFEXISTS=TRUE;ACCESS_MODE_DATA=r");
    }

    @Test
    void accettaPercorsiWindows() throws Exception {
        assertThat(url("C:\\ProgramData\\FullProjectStudio\\db\\fullprojectstudio"))
                .isEqualTo("jdbc:h2:file:C:/ProgramData/FullProjectStudio/db/fullprojectstudio;IFEXISTS=TRUE;ACCESS_MODE_DATA=r");
    }
}
