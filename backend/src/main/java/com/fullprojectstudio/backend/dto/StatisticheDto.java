package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticheDto {
    private Long stagioneId;
    private String stagioneNome;
    private List<StatisticaMeseDto> scuola;
    private List<StatisticheCorsoDto> corsi;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticheCorsoDto {
        private Long corsoId;
        private String corsoNome;
        private Integer capienzaMax;
        private boolean attivo;
        private List<StatisticaMeseDto> mesi;
    }
}
