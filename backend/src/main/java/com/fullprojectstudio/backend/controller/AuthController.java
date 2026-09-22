package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.CambioPasswordRequest;
import com.fullprojectstudio.backend.dto.LoginRequest;
import com.fullprojectstudio.backend.dto.LoginResponse;
import com.fullprojectstudio.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> cambiaPassword(@Valid @RequestBody CambioPasswordRequest request, Authentication authentication) {
        authService.cambiaPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
