package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.CorsoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CorsoApiClient {

    private final RestClient backendRestClient;

    public List<CorsoDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/corsi")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CorsoDto>>() {});
    }

    public CorsoDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/corsi/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(CorsoDto.class);
    }

    public CorsoDto create(String token, CorsoDto dto) {
        return backendRestClient.post()
                .uri("/api/corsi")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(CorsoDto.class);
    }

    public CorsoDto update(String token, Long id, CorsoDto dto) {
        return backendRestClient.put()
                .uri("/api/corsi/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(CorsoDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/corsi/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
