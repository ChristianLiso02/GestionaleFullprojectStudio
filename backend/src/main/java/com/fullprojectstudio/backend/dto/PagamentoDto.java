package com.fullprojectstudio.backend.dto;

import com.fullprojectstudio.backend.model.MetodoPagamento;
import com.fullprojectstudio.backend.model.StatoPagamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class PagamentoDto {
    private Long id;
    private Long studenteId;
    private String studenteNomeCompleto;
    private Long iscrizioneId;
    private BigDecimal importo;
    private LocalDate dataPagamento;
    private MetodoPagamento metodo;
    private String causale;

    @NotNull(message = "Indica il mese a cui si riferisce il pagamento")
    private YearMonth meseRiferimento;

    @Min(value = 1, message = "Il pagamento deve coprire almeno un mese")
    private Integer mesiCoperti;
    private StatoPagamento stato;
    private String note;
}
