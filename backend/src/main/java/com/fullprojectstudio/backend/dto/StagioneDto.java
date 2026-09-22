package com.fullprojectstudio.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagioneDto {
    private Long id;

    @NotBlank(message = "Il nome della stagione è obbligatorio")
    private String nome;

    private LocalDate dataInizio;
    private LocalDate dataFine;
    private boolean corrente;
}
