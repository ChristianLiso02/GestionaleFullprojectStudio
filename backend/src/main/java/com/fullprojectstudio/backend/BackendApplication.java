package com.fullprojectstudio.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        if (AvvioDesktop.attivo()) {
            AvvioDesktop.impostaCartellaDati();
            java.nio.file.Path esportaIn = EsportazioneMigrazione.cartellaRichiesta(args, AvvioDesktop.cartellaDati());
            if (esportaIn != null) {
                // "Esporta dati per il server": copia tutti i dati ed esce, senza avviare il gestionale.
                System.exit(EsportazioneMigrazione.esegui(AvvioDesktop.cartellaDati(), esportaIn));
            }
            boolean apri = AvvioDesktop.apriBrowser(args);
            switch (AvvioDesktop.statoPorta()) {
                case GESTIONALE_ACCESO -> {
                    // Icona cliccata con il gestionale già acceso: basta aprire il browser.
                    if (apri) AvvioDesktop.apri();
                    return;
                }
                case OCCUPATA_DA_ALTRO -> {
                    AvvioDesktop.avvisa("FullProject Studio non può partire: l'indirizzo " + AvvioDesktop.indirizzo()
                            + " è già usato da un altro programma.\n\nSe hai avviato una versione di prova del gestionale"
                            + " (ad esempio con Docker), chiudila e riapri FullProject Studio.");
                    System.exit(2);
                }
                case LIBERA -> { }
            }
            SpringApplication.run(BackendApplication.class, AvvioDesktop.senzaArgomentiDesktop(args));
            if (apri) AvvioDesktop.apri();
            return;
        }
        SpringApplication.run(BackendApplication.class, args);
    }
}
