package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.IstruttoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IstruttoreApiClient {

    private final RestClient backendRestClient;

    public List<IstruttoreDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/istruttori")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IstruttoreDto>>() {});
    }

    public IstruttoreDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/istruttori/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(IstruttoreDto.class);
    }

    public IstruttoreDto create(String token, IstruttoreDto dto) {
        return backendRestClient.post()
                .uri("/api/istruttori")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(IstruttoreDto.class);
    }

    public IstruttoreDto update(String token, Long id, IstruttoreDto dto) {
        return backendRestClient.put()
                .uri("/api/istruttori/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(IstruttoreDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/istruttori/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
