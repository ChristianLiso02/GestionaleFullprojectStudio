package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private String stagioneCorrenteNome;
    private long studentiAttivi;
    private long corsiAttivi;
    private long iscrizioniAttive;
    private BigDecimal incassiMeseCorrente;
    private long pagamentiInSospeso;
    private List<IscrizioneDto> iscrizioniInScadenza;
}
