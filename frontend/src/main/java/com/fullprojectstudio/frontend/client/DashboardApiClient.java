package com.fullprojectstudio.frontend.client;

import com.fullprojectstudio.frontend.dto.DashboardStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DashboardApiClient {

    private final RestClient backendRestClient;

    public DashboardStatsDto getStats(String token) {
        return backendRestClient.get()
                .uri("/api/dashboard/stats")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(DashboardStatsDto.class);
    }
}
