package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.PagamentoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PagamentoApiClient {

    private final RestClient backendRestClient;

    public List<PagamentoDto> findAll(String token) {
        return backendRestClient.get()
                .uri("/api/pagamenti")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PagamentoDto>>() {});
    }

    public List<PagamentoDto> findByStudente(String token, Long studenteId) {
        return backendRestClient.get()
                .uri("/api/pagamenti?studenteId={id}", studenteId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PagamentoDto>>() {});
    }

    public PagamentoDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/pagamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(PagamentoDto.class);
    }

    public PagamentoDto create(String token, PagamentoDto dto) {
        return backendRestClient.post()
                .uri("/api/pagamenti")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(PagamentoDto.class);
    }

    public PagamentoDto update(String token, Long id, PagamentoDto dto) {
        return backendRestClient.put()
                .uri("/api/pagamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(PagamentoDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/pagamenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
