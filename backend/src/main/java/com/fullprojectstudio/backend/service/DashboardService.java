package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.DashboardStatsDto;
import com.fullprojectstudio.backend.dto.IscrizioneDto;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StudenteRepository studenteRepository;
    private final CorsoRepository corsoRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final PagamentoRepository pagamentoRepository;
    private final IscrizioneService iscrizioneService;

    public DashboardStatsDto getStats() {
        LocalDate oggi = LocalDate.now();
        LocalDate inizioMese = oggi.withDayOfMonth(1);
        LocalDate fineMese = oggi.withDayOfMonth(oggi.lengthOfMonth());

        BigDecimal incassiMese = pagamentoRepository.findByDataPagamentoBetween(inizioMese, fineMese).stream()
                .filter(p -> p.getStato() == StatoPagamento.PAGATO)
                .map(Pagamento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        java.util.List<IscrizioneDto> inScadenza = iscrizioneService.findInScadenza(30);

        return DashboardStatsDto.builder()
                .studentiAttivi(studenteRepository.findByAttivoTrue().size())
                .corsiAttivi(corsoRepository.findByAttivoTrue().size())
                .iscrizioniAttive(iscrizioneRepository.findByStato(StatoIscrizione.ATTIVA).size())
                .incassiMeseCorrente(incassiMese)
                .pagamentiInSospeso(pagamentoRepository.findByStato(StatoPagamento.IN_SOSPESO).size())
                .iscrizioniInScadenza(inScadenza)
                .build();
    }
}
