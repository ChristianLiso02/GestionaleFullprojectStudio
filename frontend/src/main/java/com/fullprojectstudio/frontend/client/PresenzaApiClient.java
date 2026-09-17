package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.PresenzaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PresenzaApiClient {

    private final RestClient backendRestClient;

    public List<PresenzaDto> findByCorsoEData(String token, Long corsoId, LocalDate data) {
        return backendRestClient.get()
                .uri("/api/presenze?corsoId={corsoId}&data={data}", corsoId, data)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PresenzaDto>>() {});
    }

    public List<PresenzaDto> findByIscrizione(String token, Long iscrizioneId) {
        return backendRestClient.get()
                .uri("/api/presenze?iscrizioneId={id}", iscrizioneId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PresenzaDto>>() {});
    }

    public PresenzaDto registra(String token, PresenzaDto dto) {
        return backendRestClient.post()
                .uri("/api/presenze")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(PresenzaDto.class);
    }
}
