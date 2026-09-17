package com.fullprojectstudio.backend.dto;

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
    private String nome;
    private String cognome;
    private String codiceFiscale;
    private LocalDate dataNascita;
    private String telefono;
    private String email;
    private String indirizzo;
    private String contattoEmergenza;
    private String noteMediche;
    private LocalDate dataIscrizione;
    private boolean attivo;
}
