package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "corsi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StileBallo stile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Livello livello;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "istruttore_id")
    private Istruttore istruttore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "corso_giorni", joinColumns = @JoinColumn(name = "corso_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "giorno")
    @Builder.Default
    private Set<DayOfWeek> giorniSettimana = new HashSet<>();

    private LocalTime orarioInizio;

    private LocalTime orarioFine;

    private Integer capienzaMax;

    private BigDecimal prezzoMensile;

    private LocalDate dataInizio;

    private LocalDate dataFine;

    @Builder.Default
    private boolean attivo = true;
}
