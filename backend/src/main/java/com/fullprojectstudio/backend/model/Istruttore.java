package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "istruttori")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Istruttore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    private String telefono;

    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "istruttore_specializzazioni", joinColumns = @JoinColumn(name = "istruttore_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "stile")
    @Builder.Default
    private Set<StileBallo> specializzazioni = new HashSet<>();

    private BigDecimal compensoOrario;

    @Builder.Default
    private boolean attivo = true;
}
