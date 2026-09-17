package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "studenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Studente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(unique = true)
    private String codiceFiscale;

    private LocalDate dataNascita;

    private String telefono;

    private String email;

    private String indirizzo;

    private String contattoEmergenza;

    @Column(length = 2000)
    private String noteMediche;

    @Builder.Default
    private LocalDate dataIscrizione = LocalDate.now();

    @Builder.Default
    private boolean attivo = true;

    @Builder.Default
    private LocalDateTime dataCreazione = LocalDateTime.now();
}
