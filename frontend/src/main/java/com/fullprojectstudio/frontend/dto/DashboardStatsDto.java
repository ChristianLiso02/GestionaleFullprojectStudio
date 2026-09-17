package com.fullprojectstudio.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long studentiAttivi;
    private long corsiAttivi;
    private long iscrizioniAttive;
    private BigDecimal incassiMeseCorrente;
    private long pagamentiInSospeso;
    private List<IscrizioneDto> iscrizioniInScadenza;
}
