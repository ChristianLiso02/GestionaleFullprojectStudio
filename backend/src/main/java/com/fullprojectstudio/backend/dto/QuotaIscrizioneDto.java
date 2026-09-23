package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaIscrizioneDto {

    // In ordine di urgenza: la lista viene ordinata su questo ordine.
    public enum StatoQuota { NON_PAGATO, DA_PAGARE, PAGATO_IN_RITARDO, PAGATO_IN_TEMPO }

    private Long iscrizioneId;
    private Long studenteId;
    private String studenteNomeCompleto;
    private Long corsoId;
    private String corsoNome;
    private String tipoAbbonamentoNome;
    private BigDecimal quotaImporto;
    private int quotaMesi;
    private LocalDate scadenza;
    private StatoQuota stato;
    private Long pagamentoId;
    private LocalDate dataPagamento;
    private YearMonth coperturaFino;
}
