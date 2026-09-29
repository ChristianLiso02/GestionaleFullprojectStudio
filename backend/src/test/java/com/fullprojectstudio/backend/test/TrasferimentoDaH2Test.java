package com.fullprojectstudio.backend.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @TempDir Path cartella;

    private String database(String file) throws Exception {
        Class<?> c = Class.forName("com.fullprojectstudio.backend.config.TrasferimentoDaH2");
        Method m = c.getDeclaredMethod("databaseDa", String.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(null, file);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private Path zip(String nomeVoce) throws Exception {
        Path zip = cartella.resolve("fullprojectstudio-dati.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry(nomeVoce));
            out.write("contenuto".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    @Test
    void dalloZipEsportatoEstraeIlDatabase() throws Exception {
        String estratto = database(zip("fullprojectstudio.mv.db").toString());

        assertThat(estratto).endsWith("fullprojectstudio.mv.db");
        assertThat(Files.readString(Path.of(estratto))).isEqualTo("contenuto");
    }

    @Test
    void unFileNonZipSiUsaComE() throws Exception {
        assertThat(database("/import/fullprojectstudio.mv.db")).isEqualTo("/import/fullprojectstudio.mv.db");
    }

    @Test
    void unoZipSenzaDatabaseORinexistenteVieneRifiutato() throws Exception {
        Path sbagliato = zip("appunti.txt");
        assertThatThrownBy(() -> database(sbagliato.toString())).hasMessageContaining("non contiene un database");
        assertThatThrownBy(() -> database(cartella.resolve("manca.zip").toString())).hasMessageContaining("non trovato");
    }
}
