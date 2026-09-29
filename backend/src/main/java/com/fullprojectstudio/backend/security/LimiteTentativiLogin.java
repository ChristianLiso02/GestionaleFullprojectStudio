package com.fullprojectstudio.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blocca il login dopo troppi tentativi sbagliati dallo stesso indirizzo per lo stesso utente,
 * così una password non si può indovinare provandone migliaia. I tentativi restano in memoria:
 * un riavvio del gestionale li azzera.
 */
@Component
public class LimiteTentativiLogin {

    private final int maxTentativi;
    private final Duration finestra;
    private final Map<String, Deque<Instant>> fallimenti = new ConcurrentHashMap<>();

    public LimiteTentativiLogin(@Value("${app.login.max-tentativi:5}") int maxTentativi,
                                @Value("${app.login.minuti-blocco:15}") int minutiBlocco) {
        this.maxTentativi = maxTentativi;
        this.finestra = Duration.ofMinutes(minutiBlocco);
    }

    /** Minuti di attesa ancora necessari, oppure 0 se si può tentare il login. */
    public long minutiDiAttesa(String chiave, Instant ora) {
        Deque<Instant> tentativi = fallimenti.get(chiave);
        if (tentativi == null) return 0;
        synchronized (tentativi) {
            pulisci(tentativi, ora);
            if (tentativi.size() < maxTentativi) return 0;
            Duration resta = Duration.between(ora, tentativi.peekFirst().plus(finestra));
            return Math.max(1, (resta.getSeconds() + 59) / 60);
        }
    }

    public void registraFallimento(String chiave, Instant ora) {
        Deque<Instant> tentativi = fallimenti.computeIfAbsent(chiave, k -> new ArrayDeque<>());
        synchronized (tentativi) {
            pulisci(tentativi, ora);
            tentativi.addLast(ora);
        }
    }

    public void azzera(String chiave) {
        fallimenti.remove(chiave);
    }

    private void pulisci(Deque<Instant> tentativi, Instant ora) {
        while (!tentativi.isEmpty() && !tentativi.peekFirst().plus(finestra).isAfter(ora)) {
            tentativi.pollFirst();
        }
    }
}
