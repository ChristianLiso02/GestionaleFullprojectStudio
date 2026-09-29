package com.fullprojectstudio.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dati del proprio account: nome, cognome ed email si modificano, username e ruolo sono in sola lettura. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfiloDto {
    private String username;
    private String ruolo;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(max = 100)
    private String cognome;

    @Email(message = "Email non valida")
    @Size(max = 150)
    private String email;
}
