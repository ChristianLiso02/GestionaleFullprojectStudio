package com.fullprojectstudio.frontend.dto;

import com.fullprojectstudio.frontend.model.MetodoPagamento;
import com.fullprojectstudio.frontend.model.StatoPagamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
