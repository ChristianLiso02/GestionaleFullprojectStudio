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
            if (AvvioDesktop.giaAcceso()) {
                // Icona cliccata con il gestionale già acceso: basta aprire il browser.
                if (apri) AvvioDesktop.apri();
                return;
            }
            SpringApplication.run(BackendApplication.class, AvvioDesktop.senzaArgomentiDesktop(args));
            if (apri) AvvioDesktop.apri();
            return;
        }
        SpringApplication.run(BackendApplication.class, args);
    }
}
