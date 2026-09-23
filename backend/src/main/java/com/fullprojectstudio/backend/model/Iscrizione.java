package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // Storico dei ritiri dal corso (ogni periodo va dal ritiro al rientro).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "iscrizione_id", nullable = false)
    @OrderBy("dataRitiro ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PeriodoRitiro> ritiri = new ArrayList<>();

    public Optional<PeriodoRitiro> ritiroInCorso() {
        return ritiri.stream().filter(p -> p.getDataRientro() == null).findFirst();
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatoIscrizione stato = StatoIscrizione.ATTIVA;

    @Column(length = 1000)
    private String note;
}
