package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.dto.StatisticaMeseDto;
import com.fullprojectstudio.backend.dto.StatisticheDto;
import com.fullprojectstudio.backend.dto.StatisticheDto.StatisticheCorsoDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.PeriodoRitiro;
import com.fullprojectstudio.backend.model.Sesso;
import com.fullprojectstudio.backend.model.Stagione;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Statistiche mese per mese di una stagione, per corso e per tutta la scuola.
 * Gli incassi sono per data di pagamento; lo stato delle quote segue le regole di {@link QuoteService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticheService {

    private static final Pattern ANNO_INIZIO = Pattern.compile("^(\\d{4})");
    private static final int MAX_MESI = 24;
    // In assenza di date, una stagione "2026/2027" va da settembre ad agosto.
    private static final int MESE_INIZIO_STAGIONE = 9;

    private final StagioneRepository stagioneRepository;
    private final CorsoRepository corsoRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final PagamentoRepository pagamentoRepository;
    private final QuoteService quoteService;

    public StatisticheDto statistiche(Long stagioneId, LocalDate oggi) {
        Stagione stagione = (stagioneId != null ? stagioneRepository.findById(stagioneId) : stagioneRepository.findByCorrenteTrue())
                .orElseThrow(() -> new ResourceNotFoundException("Stagione non trovata"));

        List<Corso> corsi = corsoRepository.findByStagioneId(stagione.getId()).stream()
                .sorted(Comparator.comparing(Corso::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByCorso_StagioneId(stagione.getId());
        Map<Long, List<Pagamento>> pagamenti = iscrizioni.isEmpty() ? Map.of() : pagamentoRepository
                .findByIscrizioneIdInAndStato(iscrizioni.stream().map(Iscrizione::getId).toList(), StatoPagamento.PAGATO)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getIscrizione().getId()));
        Map<Long, List<Iscrizione>> iscrizioniPerCorso = iscrizioni.stream()
                .collect(Collectors.groupingBy(i -> i.getCorso().getId()));

        List<YearMonth> mesi = mesiDellaStagione(stagione, iscrizioni, oggi);

        List<StatisticheCorsoDto> perCorso = corsi.stream()
                .map(c -> StatisticheCorsoDto.builder()
                        .corsoId(c.getId())
                        .corsoNome(c.getNome())
                        .capienzaMax(c.getCapienzaMax())
                        .attivo(c.isAttivo())
                        .mesi(mesi.stream()
                                .map(m -> mese(m, iscrizioniPerCorso.getOrDefault(c.getId(), List.of()), pagamenti, oggi))
                                .toList())
                        .build())
                .toList();

        List<StatisticaMeseDto> scuola = mesi.stream()
                .map(m -> {
                    StatisticaMeseDto totale = mese(m, iscrizioni, pagamenti, oggi);
                    totale.setIncassi(totale.getIncassi().add(altriIncassi(m)));
                    totale.setStudentiAttivi(studentiAttivi(m, iscrizioni, pagamenti, oggi));
                    return totale;
                })
                .toList();

        return StatisticheDto.builder()
                .stagioneId(stagione.getId())
                .stagioneNome(stagione.getNome())
                .scuola(scuola)
                .corsi(perCorso)
                .build();
    }

    private StatisticaMeseDto mese(YearMonth mese, List<Iscrizione> iscrizioni, Map<Long, List<Pagamento>> pagamenti, LocalDate oggi) {
        StatisticaMeseDto s = StatisticaMeseDto.builder().mese(mese).incassi(BigDecimal.ZERO).build();
        for (Iscrizione i : iscrizioni) {
            List<Pagamento> pagamentiIscrizione = pagamenti.getOrDefault(i.getId(), List.of());

            Optional<StatoQuota> stato = quoteService.statoQuotaMese(i, mese, oggi, pagamentiIscrizione);
            if (stato.isPresent()) {
                s.setIscrittiAttivi(s.getIscrittiAttivi() + 1);
                Sesso sesso = i.getStudente().getSesso();
                if (sesso == Sesso.UOMO) s.setUomini(s.getUomini() + 1);
                if (sesso == Sesso.DONNA) s.setDonne(s.getDonne() + 1);
                switch (stato.get()) {
                    case PAGATO_IN_TEMPO -> s.setQuotePagateInTempo(s.getQuotePagateInTempo() + 1);
                    case PAGATO_IN_RITARDO -> s.setQuotePagateInRitardo(s.getQuotePagateInRitardo() + 1);
                    case SCADUTO -> s.setQuoteScadute(s.getQuoteScadute() + 1);
                    case DA_RINNOVARE -> s.setQuoteDaRinnovare(s.getQuoteDaRinnovare() + 1);
                }
            }
            if (nelMese(i.getDataIscrizione(), mese)) {
                s.setNuoveIscrizioni(s.getNuoveIscrizioni() + 1);
            }
            for (PeriodoRitiro p : i.getRitiri()) {
                if (nelMese(p.getDataRitiro(), mese)) s.setRitiri(s.getRitiri() + 1);
                if (nelMese(p.getDataRientro(), mese)) s.setRientri(s.getRientri() + 1);
            }
            for (Pagamento p : pagamentiIscrizione) {
                if (nelMese(p.getDataPagamento(), mese)) {
                    s.setIncassi(s.getIncassi().add(p.getImporto()));
                }
            }
        }
        return s;
    }

    private int studentiAttivi(YearMonth mese, List<Iscrizione> iscrizioni, Map<Long, List<Pagamento>> pagamenti, LocalDate oggi) {
        Set<Long> studenti = new HashSet<>();
        for (Iscrizione i : iscrizioni) {
            if (quoteService.statoQuotaMese(i, mese, oggi, pagamenti.getOrDefault(i.getId(), List.of())).isPresent()) {
                studenti.add(i.getStudente().getId());
            }
        }
        return studenti.size();
    }

    // Incassi non legati a un'iscrizione (es. quote associative, eventi): contano solo nel totale della scuola.
    private BigDecimal altriIncassi(YearMonth mese) {
        return pagamentoRepository
                .findByIscrizioneIsNullAndStatoAndDataPagamentoBetween(StatoPagamento.PAGATO, mese.atDay(1), mese.atEndOfMonth())
                .stream()
                .map(Pagamento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Da inizio stagione (date della stagione, oppure settembre dell'anno nel nome, anticipato alla prima
     * iscrizione se precedente) fino a fine stagione o al mese corrente, se la stagione è ancora in corso.
     */
    List<YearMonth> mesiDellaStagione(Stagione stagione, List<Iscrizione> iscrizioni, LocalDate oggi) {
        YearMonth corrente = YearMonth.from(oggi);
        YearMonth inizioStagione = stagione.getDataInizio() != null ? YearMonth.from(stagione.getDataInizio()) : annoDalNome(stagione);
        Optional<YearMonth> primaIscrizione = iscrizioni.stream()
                .map(Iscrizione::getDataIscrizione)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(YearMonth::from);

        YearMonth fine = stagione.getDataFine() != null ? YearMonth.from(stagione.getDataFine())
                : inizioStagione != null ? inizioStagione.plusMonths(11) : corrente;
        YearMonth inizio = inizioStagione;
        if (inizio == null || primaIscrizione.filter(p -> p.isBefore(inizioStagione)).isPresent()) {
            inizio = primaIscrizione.orElse(corrente);
        }
        if (fine.isAfter(corrente)) fine = corrente;
        if (fine.isBefore(inizio)) fine = inizio;
        if (inizio.plusMonths(MAX_MESI - 1L).isBefore(fine)) inizio = fine.minusMonths(MAX_MESI - 1L);

        List<YearMonth> mesi = new ArrayList<>();
        for (YearMonth m = inizio; !m.isAfter(fine); m = m.plusMonths(1)) {
            mesi.add(m);
        }
        return mesi;
    }

    private YearMonth annoDalNome(Stagione stagione) {
        Matcher m = ANNO_INIZIO.matcher(stagione.getNome() == null ? "" : stagione.getNome().trim());
        return m.find() ? YearMonth.of(Integer.parseInt(m.group(1)), MESE_INIZIO_STAGIONE) : null;
    }

    private boolean nelMese(LocalDate data, YearMonth mese) {
        return data != null && YearMonth.from(data).equals(mese);
    }
}
