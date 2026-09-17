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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatoPagamento stato = StatoPagamento.PAGATO;

    @Column(length = 1000)
    private String note;
}
