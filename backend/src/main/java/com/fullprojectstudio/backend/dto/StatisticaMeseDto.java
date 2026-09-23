package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Situazione di un mese: per un corso oppure per tutta la scuola. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticaMeseDto {
    private YearMonth mese;
    // Iscrizioni a cui è dovuta la quota del mese (iscritti e non ritirati).
    private int iscrittiAttivi;
    // Solo a livello scuola: studenti distinti (uno studente in due corsi conta una volta).
    private Integer studentiAttivi;
    private int uomini;
    private int donne;
    private int nuoveIscrizioni;
    private int ritiri;
    private int rientri;
    private BigDecimal incassi;
    private int quotePagateInTempo;
    private int quotePagateInRitardo;
    private int quoteScadute;
    private int quoteDaRinnovare;
}
