package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.LoginRequest;
import com.fullprojectstudio.frontend.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthApiClient {

    private final RestClient backendRestClient;

    public LoginResponse login(String username, String password) {
        return backendRestClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(LoginResponse.class);
    }
}
