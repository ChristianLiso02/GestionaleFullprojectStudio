package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stagioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stagione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private LocalDate dataInizio;

    private LocalDate dataFine;

    @Builder.Default
    private boolean corrente = false;

    @Builder.Default
    private LocalDateTime dataCreazione = LocalDateTime.now();
}
