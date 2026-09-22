package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.CambioPasswordRequest;
import com.fullprojectstudio.backend.dto.LoginRequest;
import com.fullprojectstudio.backend.dto.LoginResponse;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Utente;
import com.fullprojectstudio.backend.repository.UtenteRepository;
import com.fullprojectstudio.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtenteRepository utenteRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Utente utente = utenteRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        String token = jwtUtil.generateToken(utente.getUsername(), utente.getRuolo().name());

        return LoginResponse.builder()
                .token(token)
                .username(utente.getUsername())
                .nome(utente.getNome())
                .cognome(utente.getCognome())
                .ruolo(utente.getRuolo().name())
                .build();
    }

    public void cambiaPassword(String username, CambioPasswordRequest request) {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!passwordEncoder.matches(request.getVecchiaPassword(), utente.getPassword())) {
            throw new IllegalArgumentException("La password attuale non è corretta");
        }

        utente.setPassword(passwordEncoder.encode(request.getNuovaPassword()));
        utenteRepository.save(utente);
    }
}
