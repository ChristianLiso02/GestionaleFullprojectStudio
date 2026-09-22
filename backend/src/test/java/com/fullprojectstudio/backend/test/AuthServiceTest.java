package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.CambioPasswordRequest;
import com.fullprojectstudio.backend.dto.LoginRequest;
import com.fullprojectstudio.backend.dto.LoginResponse;
import com.fullprojectstudio.backend.model.Ruolo;
import com.fullprojectstudio.backend.model.Utente;
import com.fullprojectstudio.backend.repository.UtenteRepository;
import com.fullprojectstudio.backend.security.JwtUtil;
import com.fullprojectstudio.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica il login e il cambio password in AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UtenteRepository utenteRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private Utente utente() {
        return Utente.builder().id(1L).username("admin").password("hashVecchio")
                .nome("Admin").cognome("FullProject").ruolo(Ruolo.ADMIN).attivo(true).build();
    }

    @Test
    void loginRestituisceTokenERuoloDellUtente() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente()));
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("token-fittizio");

        LoginResponse risposta = authService.login(new LoginRequest("admin", "FullProject2026!"));

        assertThat(risposta.getToken()).isEqualTo("token-fittizio");
        assertThat(risposta.getRuolo()).isEqualTo("ADMIN");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void rifiutaCambioPasswordSeLaVecchiaEErrata() {
        Utente utente = utente();
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("sbagliata", "hashVecchio")).thenReturn(false);

        CambioPasswordRequest request = CambioPasswordRequest.builder()
                .vecchiaPassword("sbagliata").nuovaPassword("NuovaPassword123").build();

        assertThatThrownBy(() -> authService.cambiaPassword("admin", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password attuale non è corretta");

        verify(utenteRepository, never()).save(any());
    }

    @Test
    void aggiornaLaPasswordSeLaVecchiaECorretta() {
        Utente utente = utente();
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("FullProject2026!", "hashVecchio")).thenReturn(true);
        when(passwordEncoder.encode("NuovaPassword123")).thenReturn("hashNuovo");

        CambioPasswordRequest request = CambioPasswordRequest.builder()
                .vecchiaPassword("FullProject2026!").nuovaPassword("NuovaPassword123").build();

        authService.cambiaPassword("admin", request);

        assertThat(utente.getPassword()).isEqualTo("hashNuovo");
        verify(utenteRepository).save(utente);
    }
}
