package com.fullprojectstudio.backend.config;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import com.fullprojectstudio.backend.security.PasswordCasuale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * Prepara un'installazione nuova: gli utenti di accesso e la stagione corrente.
 * Sale, istruttori, abbonamenti e corsi li inserisce la segreteria dal gestionale.
 * Se per un utente non è configurata una password (installazione su PC), ne crea una casuale
 * e la scrive in credenziali-iniziali.txt nella cartella dei dati.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtenteRepository utenteRepository;
    private final CorsoRepository corsoRepository;
    private final StagioneRepository stagioneRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password:}")
    private String adminPassword;
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.segreteria.username}")
    private String segreteriaUsername;
    @Value("${app.segreteria.password:}")
    private String segreteriaPassword;
    @Value("${app.segreteria.email}")
    private String segreteriaEmail;

    @Value("${app.data-dir:./data}")
    private String cartellaDati;
    @Value("${server.port:8080}")
    private int porta;

    @Override
    public void run(String... args) {
        boolean creaAdmin = !utenteRepository.existsByUsername(adminUsername);
        boolean creaSegreteria = !utenteRepository.existsByUsername(segreteriaUsername);

        // Le password generate si scrivono su file PRIMA di creare gli utenti: se il file non si riesce a
        // scrivere ci si ferma, invece di lasciare utenti con una password che nessuno conosce.
        List<String> credenzialiGenerate = new ArrayList<>();
        String passwordAdmin = creaAdmin ? passwordOGenerata(adminPassword, "Amministratore", adminUsername, credenzialiGenerate) : null;
        String passwordSegreteria = creaSegreteria ? passwordOGenerata(segreteriaPassword, "Segreteria", segreteriaUsername, credenzialiGenerate) : null;
        scriviCredenzialiGenerate(credenzialiGenerate);

        if (creaAdmin) {
            utenteRepository.save(Utente.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(passwordAdmin))
                    .nome("Amministratore")
                    .cognome("FullProject Studio")
                    .email(adminEmail)
                    .ruolo(Ruolo.ADMIN)
                    .attivo(true)
                    .build());
        }

        if (creaSegreteria) {
            utenteRepository.save(Utente.builder()
                    .username(segreteriaUsername)
                    .password(passwordEncoder.encode(passwordSegreteria))
                    .nome("Segreteria")
                    .cognome("FullProject Studio")
                    .email(segreteriaEmail)
                    .ruolo(Ruolo.SEGRETERIA)
                    .attivo(true)
                    .build());
        }

        // Garantisce che esista sempre una stagione corrente: se manca (primo avvio in assoluto,
        // o aggiornamento di un'installazione creata prima dell'introduzione delle stagioni),
        // ne crea una col nome calcolato dalla data odierna.
        Stagione stagioneCorrente = stagioneRepository.findByCorrenteTrue()
                .orElseGet(() -> stagioneRepository.save(Stagione.builder()
                        .nome(calcolaNomeStagioneCorrente())
                        .corrente(true)
                        .build()));

        // Backfill: eventuali corsi già esistenti (creati prima di questa funzionalità)
        // senza stagione assegnata vengono agganciati a quella corrente.
        corsoRepository.findAll().stream()
                .filter(c -> c.getStagione() == null)
                .forEach(c -> {
                    c.setStagione(stagioneCorrente);
                    corsoRepository.save(c);
                });
    }

    private String passwordOGenerata(String configurata, String ruolo, String username, List<String> credenzialiGenerate) {
        if (configurata != null && !configurata.isBlank()) {
            return configurata;
        }
        String generata = PasswordCasuale.genera(12);
        credenzialiGenerate.add(ruolo + ":  utente  " + username + "   password  " + generata);
        return generata;
    }

    private void scriviCredenzialiGenerate(List<String> credenziali) {
        if (credenziali.isEmpty()) {
            return;
        }
        Path file = Path.of(cartellaDati, "credenziali-iniziali.txt");
        String testo = "FullProject Studio - credenziali di accesso\r\n"
                + "===========================================\r\n\r\n"
                + "Indirizzo:  http://localhost:" + porta + "\r\n\r\n"
                + String.join("\r\n", credenziali) + "\r\n\r\n"
                + "IMPORTANTE: dopo il primo accesso cambia le password con il pulsante \"Cambia password\"\r\n"
                + "in alto a destra, poi CANCELLA questo file.\r\n";
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, testo, StandardCharsets.UTF_8);
            log.info("Password iniziali create: le trovi in {}", file.toAbsolutePath());
        } catch (IOException e) {
            // Senza il file le password generate andrebbero perse.
            throw new IllegalStateException("Impossibile scrivere " + file.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private String calcolaNomeStagioneCorrente() {
        LocalDate oggi = LocalDate.now();
        int anno = oggi.getYear();
        return oggi.getMonthValue() >= 9 ? anno + "/" + (anno + 1) : (anno - 1) + "/" + anno;
    }
}
