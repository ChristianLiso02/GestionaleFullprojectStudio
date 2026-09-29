package com.fullprojectstudio.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/**
 * Avvio del gestionale installato su PC Windows (profilo "desktop").
 * <ul>
 *   <li>Icona sul desktop ({@code --apri}): se il gestionale è già acceso apre solo il browser, altrimenti lo
 *       avvia e poi apre il browser.</li>
 *   <li>Avvio automatico all'accensione (nessun argomento): lo avvia senza aprire il browser; se è già acceso
 *       non fa niente.</li>
 * </ul>
 */
final class AvvioDesktop {

    static final String APRI_BROWSER = "--apri";
    private static final int PORTA_PREDEFINITA = 8081;

    private AvvioDesktop() {
    }

    static boolean attivo() {
        String profili = System.getProperty("spring.profiles.active",
                System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", ""));
        return Arrays.asList(profili.split(",")).contains("desktop");
    }

    static boolean apriBrowser(String[] args) {
        return Arrays.asList(args).contains(APRI_BROWSER);
    }

    static String[] senzaArgomentiDesktop(String[] args) {
        return Arrays.stream(args).filter(a -> !a.equals(APRI_BROWSER)).toArray(String[]::new);
    }

    /** Rende disponibile a application.yml la cartella dati, con le "/" anche su Windows. */
    static void impostaCartellaDati() {
        System.setProperty("fps.data-dir", cartellaDati().toAbsolutePath().toString().replace('\\', '/'));
    }

    static String indirizzo() {
        return "http://localhost:" + porta();
    }

    /** La porta può essere cambiata nel file di configurazione della cartella dati (come legge Spring). */
    static int porta() {
        String daAmbiente = System.getenv("PORTA");
        if (daAmbiente != null && !daAmbiente.isBlank()) {
            return Integer.parseInt(daAmbiente.trim());
        }
        Path config = cartellaDati().resolve("config").resolve("application.properties");
        if (Files.isRegularFile(config)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(config)) {
                p.load(in);
                String porta = p.getProperty("server.port");
                if (porta != null && !porta.isBlank()) {
                    return Integer.parseInt(porta.trim());
                }
            } catch (IOException | NumberFormatException e) {
                // File illeggibile o porta non valida: si prova con quella predefinita.
            }
        }
        return PORTA_PREDEFINITA;
    }

    static Path cartellaDati() {
        String dir = System.getenv("FPS_DATA_DIR");
        if (dir == null || dir.isBlank()) {
            String programData = System.getenv("ProgramData");
            dir = (programData != null ? programData : ".") + "/FullProjectStudio";
        }
        return Path.of(dir);
    }

    enum StatoPorta { LIBERA, GESTIONALE_ACCESO, OCCUPATA_DA_ALTRO }

    /**
     * Chi risponde sulla porta del gestionale: nessuno (si può avviare), questo stesso gestionale installato
     * (basta aprire il browser) oppure un altro programma, ad esempio una versione di prova avviata con Docker.
     */
    static StatoPorta statoPorta() {
        try {
            HttpURLConnection c = (HttpURLConnection) URI.create(indirizzo() + "/api/sistema").toURL().openConnection();
            c.setConnectTimeout(1500);
            c.setReadTimeout(3000);
            try {
                if (c.getResponseCode() != 200) return StatoPorta.OCCUPATA_DA_ALTRO;
                String risposta = new String(c.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return risposta.contains("FullProjectStudio") && risposta.contains("desktop")
                        ? StatoPorta.GESTIONALE_ACCESO : StatoPorta.OCCUPATA_DA_ALTRO;
            } finally {
                c.disconnect();
            }
        } catch (java.net.ConnectException e) {
            return StatoPorta.LIBERA;
        } catch (IOException e) {
            // Qualcosa ha accettato la connessione ma non risponde come il gestionale
            return StatoPorta.OCCUPATA_DA_ALTRO;
        }
    }

    /** Finestra di errore per la segreteria (il programma non ha una console dove scrivere). */
    static void avvisa(String messaggio) {
        System.err.println(messaggio);
        if (java.awt.GraphicsEnvironment.isHeadless()) return;
        try {
            javax.swing.JOptionPane.showMessageDialog(null, messaggio, "FullProject Studio", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            // senza interfaccia grafica resta il messaggio sulla console
        }
    }

    static void apri() {
        String url = indirizzo();
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception e) {
            // si prova con il comando di Windows
        }
        try {
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
        } catch (IOException e) {
            System.err.println("Apri il browser su " + url);
        }
    }
}
