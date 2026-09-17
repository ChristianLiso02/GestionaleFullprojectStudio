package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tipi_abbonamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAbbonamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descrizione;

    private Integer durataGiorni;

    private Integer numeroLezioni;

    @Column(nullable = false)
    private BigDecimal prezzo;

    @Builder.Default
    private boolean attivo = true;
}
