package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.SalaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SalaApiClient {

    private final RestClient backendRestClient;

    public List<SalaDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/sale")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<SalaDto>>() {});
    }

    public SalaDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/sale/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(SalaDto.class);
    }

    public SalaDto create(String token, SalaDto dto) {
        return backendRestClient.post()
                .uri("/api/sale")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(SalaDto.class);
    }

    public SalaDto update(String token, Long id, SalaDto dto) {
        return backendRestClient.put()
                .uri("/api/sale/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(SalaDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/sale/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
