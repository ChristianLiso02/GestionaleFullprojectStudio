package com.fullprojectstudio.backend.security;

import com.fullprojectstudio.backend.model.Utente;
import com.fullprojectstudio.backend.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

        return new User(
                utente.getUsername(),
                utente.getPassword(),
                utente.isAttivo(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name()))
        );
    }
}
