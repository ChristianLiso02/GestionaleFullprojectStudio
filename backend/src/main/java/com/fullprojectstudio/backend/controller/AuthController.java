package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.CambioPasswordRequest;
import com.fullprojectstudio.backend.dto.LoginRequest;
import com.fullprojectstudio.backend.dto.LoginResponse;
import com.fullprojectstudio.backend.dto.ProfiloDto;
import com.fullprojectstudio.backend.exception.TroppiTentativiException;
import com.fullprojectstudio.backend.security.LimiteTentativiLogin;
import com.fullprojectstudio.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LimiteTentativiLogin limiteTentativi;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String chiave = indirizzoClient(http) + "|" + String.valueOf(request.getUsername()).toLowerCase(Locale.ROOT);
        long attesa = limiteTentativi.minutiDiAttesa(chiave, Instant.now());
        if (attesa > 0) {
            throw new TroppiTentativiException("Troppi tentativi sbagliati. Riprova tra " + attesa + (attesa == 1 ? " minuto." : " minuti."));
        }
        try {
            LoginResponse risposta = authService.login(request);
            limiteTentativi.azzera(chiave);
            return ResponseEntity.ok(risposta);
        } catch (BadCredentialsException e) {
            limiteTentativi.registraFallimento(chiave, Instant.now());
            throw e;
        }
    }

    // Sul server il gestionale sta dietro Caddy, che mette l'indirizzo reale del browser in X-Forwarded-For
    // (scartando quello eventualmente inviato dal browser stesso).
    private String indirizzoClient(HttpServletRequest http) {
        String inoltrato = http.getHeader("X-Forwarded-For");
        if (inoltrato != null && !inoltrato.isBlank()) {
            return inoltrato.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    @GetMapping("/profilo")
    public ProfiloDto profilo(Authentication authentication) {
        return authService.profilo(authentication.getName());
    }

    @PutMapping("/profilo")
    public ProfiloDto aggiornaProfilo(@Valid @RequestBody ProfiloDto dto, Authentication authentication) {
        return authService.aggiornaProfilo(authentication.getName(), dto);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> cambiaPassword(@Valid @RequestBody CambioPasswordRequest request, Authentication authentication) {
        authService.cambiaPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
