package com.fullprojectstudio.backend.dto;

import com.fullprojectstudio.backend.model.StileBallo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @Pattern(regexp = "^$|^[0-9+\\s()-]{8,20}$", message = "Numero di telefono non valido")
    private String telefono;

    @Email(message = "Email non valida")
    private String email;

    private Set<StileBallo> specializzazioni;
    private BigDecimal compensoOrario;
    private boolean attivo;
}
