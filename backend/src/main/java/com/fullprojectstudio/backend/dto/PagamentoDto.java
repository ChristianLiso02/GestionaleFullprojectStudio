package com.fullprojectstudio.backend.dto;

import com.fullprojectstudio.backend.model.MetodoPagamento;
import com.fullprojectstudio.backend.model.StatoPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoDto {
    private Long id;
    private Long studenteId;
    private String studenteNomeCompleto;
    private Long iscrizioneId;
    private BigDecimal importo;
    private LocalDate dataPagamento;
    private MetodoPagamento metodo;
    private String causale;
    private StatoPagamento stato;
    private String note;
}
