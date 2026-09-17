package com.fullprojectstudio.frontend.dto;

import com.fullprojectstudio.frontend.model.StileBallo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IstruttoreDto {
    private Long id;
    private String nome;
    private String cognome;
    private String telefono;
    private String email;
    private Set<StileBallo> specializzazioni = new HashSet<>();
    private BigDecimal compensoOrario;
    private boolean attivo;
}
