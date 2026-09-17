package com.fullprojectstudio.backend.config;

import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!utenteRepository.existsByUsername("admin")) {
            utenteRepository.save(Utente.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("FullProject2026!"))
                    .nome("Amministratore")
                    .cognome("FullProject Studio")
                    .email("admin@fullprojectstudio.it")
                    .ruolo(Ruolo.ADMIN)
                    .attivo(true)
                    .build());
        }

        if (!utenteRepository.existsByUsername("segreteria")) {
            utenteRepository.save(Utente.builder()
                    .username("segreteria")
                    .password(passwordEncoder.encode("Segreteria2026!"))
                    .nome("Segreteria")
                    .cognome("FullProject Studio")
                    .email("segreteria@fullprojectstudio.it")
                    .ruolo(Ruolo.SEGRETERIA)
                    .attivo(true)
                    .build());
        }

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
                    .istruttore(m1).sala(salaA)
                    .giorniSettimana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                    .orarioInizio(LocalTime.of(19, 0)).orarioFine(LocalTime.of(20, 0))
                    .capienzaMax(25).prezzoMensile(new BigDecimal("60.00")).attivo(true).build());

            corsoRepository.save(Corso.builder()
                    .nome("Bachata Intermedio").stile(StileBallo.BACHATA).livello(Livello.INTERMEDIO)
                    .istruttore(m2).sala(salaB)
                    .giorniSettimana(Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
                    .orarioInizio(LocalTime.of(20, 0)).orarioFine(LocalTime.of(21, 0))
                    .capienzaMax(15).prezzoMensile(new BigDecimal("60.00")).attivo(true).build());
        }
    }
}
