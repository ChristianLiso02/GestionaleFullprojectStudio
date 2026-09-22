package com.fullprojectstudio.backend.dto;

import com.fullprojectstudio.backend.model.Livello;
import com.fullprojectstudio.backend.model.StileBallo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorsoDto {
    private Long id;
    private String nome;
    private StileBallo stile;
    private Livello livello;
    private List<Long> istruttoriIds;
    private List<String> istruttoriNomi;
    private Long salaId;
    private String salaNome;
    private Long stagioneId;
    private String stagioneNome;
    private Set<DayOfWeek> giorniSettimana;
    private LocalTime orarioInizio;
    private LocalTime orarioFine;
    private Integer capienzaMax;
    private int iscrittiAttivi;
    private BigDecimal prezzoMensile;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private boolean attivo;
}
