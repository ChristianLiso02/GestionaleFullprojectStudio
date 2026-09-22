package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.DashboardStatsDto;
import com.fullprojectstudio.backend.dto.IscrizioneDto;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.Stagione;
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
    private final StagioneRepository stagioneRepository;

    public DashboardStatsDto getStats() {
        LocalDate oggi = LocalDate.now();
        LocalDate inizioMese = oggi.withDayOfMonth(1);
        LocalDate fineMese = oggi.withDayOfMonth(oggi.lengthOfMonth());

        BigDecimal incassiMese = pagamentoRepository.findByDataPagamentoBetween(inizioMese, fineMese).stream()
                .filter(p -> p.getStato() == StatoPagamento.PAGATO)
                .map(Pagamento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        java.util.List<IscrizioneDto> inScadenza = iscrizioneService.findInScadenza(30);

        java.util.Optional<Stagione> stagioneCorrente = stagioneRepository.findByCorrenteTrue();
        long corsiAttivi = stagioneCorrente
                .map(s -> corsoRepository.countByStagioneIdAndAttivoTrue(s.getId()))
                .orElseGet(() -> (long) corsoRepository.findByAttivoTrue().size());
        long iscrizioniAttive = stagioneCorrente
                .map(s -> iscrizioneRepository.countByCorso_StagioneIdAndStato(s.getId(), StatoIscrizione.ATTIVA))
                .orElseGet(() -> (long) iscrizioneRepository.findByStato(StatoIscrizione.ATTIVA).size());

        return DashboardStatsDto.builder()
                .stagioneCorrenteNome(stagioneCorrente.map(Stagione::getNome).orElse(null))
                .studentiAttivi(studenteRepository.findByAttivoTrue().size())
                .corsiAttivi(corsiAttivi)
                .iscrizioniAttive(iscrizioniAttive)
                .incassiMeseCorrente(incassiMese)
                .pagamentiInSospeso(pagamentoRepository.findByStato(StatoPagamento.IN_SOSPESO).size())
                .iscrizioniInScadenza(inScadenza)
                .build();
    }
}
