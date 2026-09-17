package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.IscrizioneDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IscrizioneApiClient {

    private final RestClient backendRestClient;

    public List<IscrizioneDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/iscrizioni")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IscrizioneDto>>() {});
    }

    public List<IscrizioneDto> findByStudente(String token, Long studenteId) {
        return backendRestClient.get()
                .uri("/api/iscrizioni?studenteId={id}", studenteId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IscrizioneDto>>() {});
    }

    public IscrizioneDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/iscrizioni/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(IscrizioneDto.class);
    }

    public IscrizioneDto create(String token, IscrizioneDto dto) {
        return backendRestClient.post()
                .uri("/api/iscrizioni")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(IscrizioneDto.class);
    }

    public IscrizioneDto update(String token, Long id, IscrizioneDto dto) {
        return backendRestClient.put()
                .uri("/api/iscrizioni/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(IscrizioneDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/iscrizioni/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
