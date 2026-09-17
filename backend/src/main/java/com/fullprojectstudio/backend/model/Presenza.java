package com.fullprojectstudio.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "presenze", uniqueConstraints = @UniqueConstraint(columnNames = {"iscrizione_id", "dataLezione"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iscrizione_id")
    private Iscrizione iscrizione;

    @Column(nullable = false)
    private LocalDate dataLezione;

    @Builder.Default
    private boolean presente = true;

    private String note;
}
