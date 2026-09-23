package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteMeseDto {
    private YearMonth mese;
    private int giornoScadenza;
    private List<QuotaIscrizioneDto> righe;
}
