package com.fullprojectstudio.frontend.dto;

import com.fullprojectstudio.frontend.model.Livello;
import com.fullprojectstudio.frontend.model.StileBallo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorsoDto {
    private Long id;
    private String nome;
    private StileBallo stile;
    private Livello livello;
    private Long istruttoreId;
    private String istruttoreNome;
    private Long salaId;
    private String salaNome;
    private Set<DayOfWeek> giorniSettimana = new HashSet<>();
    private LocalTime orarioInizio;
    private LocalTime orarioFine;
    private Integer capienzaMax;
    private BigDecimal prezzoMensile;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private boolean attivo;
}
