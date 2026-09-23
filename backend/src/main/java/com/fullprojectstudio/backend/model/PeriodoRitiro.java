package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Periodo in cui lo studente ha lasciato il corso: dataRientro è null finché non torna. */
@Entity
@Table(name = "iscrizione_ritiri")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoRitiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataRitiro;

    private LocalDate dataRientro;
}
