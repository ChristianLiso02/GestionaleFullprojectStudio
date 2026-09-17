package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "iscrizioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studente_id")
    private Studente studente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corso_id")
    private Corso corso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_abbonamento_id")
    private TipoAbbonamento tipoAbbonamento;

    @Builder.Default
    private LocalDate dataIscrizione = LocalDate.now();

    private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatoIscrizione stato = StatoIscrizione.ATTIVA;

    @Column(length = 1000)
    private String note;
}
