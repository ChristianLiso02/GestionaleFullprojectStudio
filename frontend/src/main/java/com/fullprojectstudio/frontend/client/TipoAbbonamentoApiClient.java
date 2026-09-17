package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.TipoAbbonamentoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TipoAbbonamentoApiClient {

    private final RestClient backendRestClient;

    public List<TipoAbbonamentoDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/abbonamenti")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TipoAbbonamentoDto>>() {});
    }

    public TipoAbbonamentoDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/abbonamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TipoAbbonamentoDto.class);
    }

    public TipoAbbonamentoDto create(String token, TipoAbbonamentoDto dto) {
        return backendRestClient.post()
                .uri("/api/abbonamenti")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(TipoAbbonamentoDto.class);
    }

    public TipoAbbonamentoDto update(String token, Long id, TipoAbbonamentoDto dto) {
        return backendRestClient.put()
                .uri("/api/abbonamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(TipoAbbonamentoDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/abbonamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
