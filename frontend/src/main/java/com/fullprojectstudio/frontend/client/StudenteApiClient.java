package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.StudenteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StudenteApiClient {

    private final RestClient backendRestClient;

    public List<StudenteDto> findAll(String token, String ricerca) {
        String uri = (ricerca != null && !ricerca.isBlank())
                ? "/api/studenti?ricerca=" + ricerca
                : "/api/studenti";
        return backendRestClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<StudenteDto>>() {});
    }

    public StudenteDto findById(String token, Long id) {
        return backendRestClient.get()
                .uri("/api/studenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(StudenteDto.class);
    }

    public StudenteDto create(String token, StudenteDto dto) {
        return backendRestClient.post()
                .uri("/api/studenti")
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(StudenteDto.class);
    }

    public StudenteDto update(String token, Long id, StudenteDto dto) {
        return backendRestClient.put()
                .uri("/api/studenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .body(dto)
                .retrieve()
                .body(StudenteDto.class);
    }

    public void delete(String token, Long id) {
        backendRestClient.delete()
                .uri("/api/studenti/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}
