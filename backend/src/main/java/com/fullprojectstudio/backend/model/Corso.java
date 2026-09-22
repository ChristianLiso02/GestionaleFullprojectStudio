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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "corso_istruttori",
            joinColumns = @JoinColumn(name = "corso_id"),
            inverseJoinColumns = @JoinColumn(name = "istruttore_id")
    )
    @Builder.Default
    private Set<Istruttore> istruttori = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    // Nullable a livello di schema per compatibilità con installazioni esistenti
    // (DataInitializer assegna automaticamente una stagione ai corsi che non ce l'hanno).
    // Il vincolo "sempre presente" è applicato a livello applicativo in CorsoService.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stagione_id")
    private Stagione stagione;

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
