package com.fullprojectstudio.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudenteDto {
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @Pattern(regexp = "^$|^[A-Za-z]{6}[0-9]{2}[A-Za-z][0-9]{2}[A-Za-z][0-9]{3}[A-Za-z]$",
            message = "Codice fiscale non valido")
    private String codiceFiscale;

    private LocalDate dataNascita;

    @Pattern(regexp = "^$|^[0-9+\\s()-]{8,20}$", message = "Numero di telefono non valido")
    private String telefono;

    @Email(message = "Email non valida")
    private String email;

    private String indirizzo;
    private String contattoEmergenza;
    private String noteMediche;
    private LocalDate dataIscrizione;
    private boolean attivo;
}
