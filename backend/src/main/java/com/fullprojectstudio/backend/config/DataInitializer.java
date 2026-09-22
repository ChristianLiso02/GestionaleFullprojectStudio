package com.fullprojectstudio.backend.config;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtenteRepository utenteRepository;
    private final SalaRepository salaRepository;
    private final IstruttoreRepository istruttoreRepository;
    private final TipoAbbonamentoRepository tipoAbbonamentoRepository;
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

        if (salaRepository.count() == 0) {
            Sala salaA = salaRepository.save(Sala.builder().nome("Sala Rossa").capienza(25).note("Sala principale con specchi").build());
            Sala salaB = salaRepository.save(Sala.builder().nome("Sala Nera").capienza(15).note("Sala per lezioni private").build());

            Istruttore m1 = istruttoreRepository.save(Istruttore.builder()
                    .nome("Marco").cognome("Rossi").telefono("3331234567").email("marco.rossi@fullprojectstudio.it")
                    .specializzazioni(Set.of(StileBallo.SALSA_CUBANA, StileBallo.RUEDA_DE_CASINO))
                    .compensoOrario(new BigDecimal("25.00")).attivo(true).build());

            Istruttore m2 = istruttoreRepository.save(Istruttore.builder()
                    .nome("Giulia").cognome("Bianchi").telefono("3339876543").email("giulia.bianchi@fullprojectstudio.it")
                    .specializzazioni(Set.of(StileBallo.BACHATA, StileBallo.SALSA_LOS_ANGELES))
                    .compensoOrario(new BigDecimal("28.00")).attivo(true).build());

            if (tipoAbbonamentoRepository.count() == 0) {
                tipoAbbonamentoRepository.save(TipoAbbonamento.builder()
                        .nome("Mensile").descrizione("Abbonamento mensile, corsi illimitati")
                        .durataGiorni(30).prezzo(new BigDecimal("60.00")).attivo(true).build());
                tipoAbbonamentoRepository.save(TipoAbbonamento.builder()
                        .nome("Trimestrale").descrizione("Abbonamento trimestrale, corsi illimitati")
                        .durataGiorni(90).prezzo(new BigDecimal("160.00")).attivo(true).build());
                tipoAbbonamentoRepository.save(TipoAbbonamento.builder()
                        .nome("Annuale").descrizione("Abbonamento annuale, corsi illimitati")
                        .durataGiorni(365).prezzo(new BigDecimal("550.00")).attivo(true).build());
                tipoAbbonamentoRepository.save(TipoAbbonamento.builder()
                        .nome("Pacchetto 10 lezioni").descrizione("10 lezioni da utilizzare liberamente")
                        .numeroLezioni(10).prezzo(new BigDecimal("120.00")).attivo(true).build());
            }

            corsoRepository.save(Corso.builder()
                    .nome("Salsa Cubana Base").stile(StileBallo.SALSA_CUBANA).livello(Livello.BASE)
                    .istruttori(Set.of(m1, m2)).sala(salaA).stagione(stagioneCorrente)
                    .giorniSettimana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                    .orarioInizio(LocalTime.of(19, 0)).orarioFine(LocalTime.of(20, 0))
                    .capienzaMax(25).prezzoMensile(new BigDecimal("60.00")).attivo(true).build());

            corsoRepository.save(Corso.builder()
                    .nome("Bachata Intermedio").stile(StileBallo.BACHATA).livello(Livello.INTERMEDIO)
                    .istruttori(Set.of(m2)).sala(salaB).stagione(stagioneCorrente)
                    .giorniSettimana(Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
                    .orarioInizio(LocalTime.of(20, 0)).orarioFine(LocalTime.of(21, 0))
                    .capienzaMax(15).prezzoMensile(new BigDecimal("60.00")).attivo(true).build());
        }
    }

    private String calcolaNomeStagioneCorrente() {
        LocalDate oggi = LocalDate.now();
        int anno = oggi.getYear();
        return oggi.getMonthValue() >= 9 ? anno + "/" + (anno + 1) : (anno - 1) + "/" + anno;
    }
}
