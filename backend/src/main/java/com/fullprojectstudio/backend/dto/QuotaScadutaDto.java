package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Iscrizione attiva con quote scadute: o ha pagato e va registrato, o si è ritirata senza avvisare. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaScadutaDto {
    private Long iscrizioneId;
    private Long studenteId;
    private String studenteNomeCompleto;
    private Long corsoId;
    private String corsoNome;
    private String tipoAbbonamentoNome;
    private BigDecimal quotaImporto;
    private List<YearMonth> mesiScaduti;
    private LocalDate ultimoPagamento;
    private LocalDate dataRitiroProposta;
}
