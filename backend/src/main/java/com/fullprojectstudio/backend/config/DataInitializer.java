package com.fullprojectstudio.backend.config;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Prepara un'installazione nuova: gli utenti di accesso e la stagione corrente.
 * Sale, istruttori, abbonamenti e corsi li inserisce la segreteria dal gestionale.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtenteRepository utenteRepository;
    private final CorsoRepository corsoRepository;
    private final StagioneRepository stagioneRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.segreteria.username}")
    private String segreteriaUsername;
    @Value("${app.segreteria.password}")
    private String segreteriaPassword;
    @Value("${app.segreteria.email}")
    private String segreteriaEmail;

    @Override
    public void run(String... args) {
        if (!utenteRepository.existsByUsername(adminUsername)) {
            utenteRepository.save(Utente.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .nome("Amministratore")
                    .cognome("FullProject Studio")
                    .email(adminEmail)
                    .ruolo(Ruolo.ADMIN)
                    .attivo(true)
                    .build());
        }

        if (!utenteRepository.existsByUsername(segreteriaUsername)) {
            utenteRepository.save(Utente.builder()
                    .username(segreteriaUsername)
                    .password(passwordEncoder.encode(segreteriaPassword))
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

    private String calcolaNomeStagioneCorrente() {
        LocalDate oggi = LocalDate.now();
        int anno = oggi.getYear();
        return oggi.getMonthValue() >= 9 ? anno + "/" + (anno + 1) : (anno - 1) + "/" + anno;
    }
}
