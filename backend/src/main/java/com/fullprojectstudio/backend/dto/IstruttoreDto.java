package com.fullprojectstudio.backend.dto;

import com.fullprojectstudio.backend.model.StileBallo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IstruttoreDto {
    private Long id;
    private String nome;
    private String cognome;
    private String telefono;
    private String email;
    private Set<StileBallo> specializzazioni;
    private BigDecimal compensoOrario;
    private boolean attivo;
}
