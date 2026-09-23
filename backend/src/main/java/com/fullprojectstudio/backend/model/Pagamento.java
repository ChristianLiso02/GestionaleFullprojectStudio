package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagamenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studente_id")
    private Studente studente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iscrizione_id")
    private Iscrizione iscrizione;

    @Column(nullable = false)
    private BigDecimal importo;

    @Builder.Default
    private LocalDate dataPagamento = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    private String causale;

    // Primo giorno del mese a cui si riferisce la quota. Nullable per i pagamenti registrati prima di questo campo.
    private LocalDate meseRiferimento;

    // Mesi coperti a partire da meseRiferimento (1 = mensile, 3 = trimestrale). Null equivale a 1.
    private Integer mesiCoperti;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatoPagamento stato = StatoPagamento.PAGATO;

    @Column(length = 1000)
    private String note;
}
