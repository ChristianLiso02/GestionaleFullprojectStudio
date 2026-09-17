package com.fullprojectstudio.frontend.dto;

import com.fullprojectstudio.frontend.model.StatoIscrizione;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IscrizioneDto {
    private Long id;
    private Long studenteId;
    private String studenteNomeCompleto;
    private Long corsoId;
    private String corsoNome;
    private Long tipoAbbonamentoId;
    private String tipoAbbonamentoNome;
    private LocalDate dataIscrizione;
    private LocalDate dataScadenza;
    private StatoIscrizione stato;
    private String note;
}
