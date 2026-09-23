package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto;
import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.dto.QuoteMeseDto;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.Stagione;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.model.TipoAbbonamento;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calcola, per un mese, se ogni iscrizione attiva ha la quota pagata e se in tempo.
 * Regola: la quota va pagata entro il giorno {@code giornoScadenza} del mese; chi si iscrive
 * dopo quel giorno paga al momento dell'iscrizione. Un pagamento copre i mesi da
 * meseRiferimento per mesiCoperti mesi (es. trimestrale = 3).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final IscrizioneRepository iscrizioneRepository;
    private final PagamentoRepository pagamentoRepository;
    private final StagioneRepository stagioneRepository;

    @Value("${app.pagamenti.giorno-scadenza:7}")
    private int giornoScadenza;

    public QuoteMeseDto situazione(YearMonth mese, Long stagioneId, LocalDate oggi) {
        Long idStagione = stagioneId != null
                ? stagioneId
                : stagioneRepository.findByCorrenteTrue().map(Stagione::getId).orElse(null);
        List<QuotaIscrizioneDto> righe = idStagione == null ? List.of() : calcolaRighe(mese, idStagione, oggi);
        return QuoteMeseDto.builder().mese(mese).giornoScadenza(giornoScadenza).righe(righe).build();
    }

    public long contaNonPagate(YearMonth mese, LocalDate oggi) {
        return situazione(mese, null, oggi).getRighe().stream()
                .filter(r -> r.getStato() == StatoQuota.NON_PAGATO)
                .count();
    }

    private List<QuotaIscrizioneDto> calcolaRighe(YearMonth mese, Long stagioneId, LocalDate oggi) {
        LocalDate fineMese = mese.atEndOfMonth();
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByCorso_StagioneId(stagioneId).stream()
                .filter(i -> i.getStato() == StatoIscrizione.ATTIVA)
                .filter(i -> i.getDataIscrizione() == null || !i.getDataIscrizione().isAfter(fineMese))
                .toList();
        if (iscrizioni.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Pagamento>> pagamentiPerIscrizione = pagamentoRepository
                .findByIscrizioneIdInAndStato(iscrizioni.stream().map(Iscrizione::getId).toList(), StatoPagamento.PAGATO)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getIscrizione().getId()));

        return iscrizioni.stream()
                .map(i -> riga(i, mese, oggi, pagamentiPerIscrizione.getOrDefault(i.getId(), List.of())))
                .sorted(Comparator.comparing(QuotaIscrizioneDto::getStato)
                        .thenComparing(QuotaIscrizioneDto::getStudenteNomeCompleto, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private QuotaIscrizioneDto riga(Iscrizione i, YearMonth mese, LocalDate oggi, List<Pagamento> pagamenti) {
        LocalDate scadenza = scadenza(mese, i.getDataIscrizione());
        Optional<Pagamento> pagamento = pagamenti.stream()
                .filter(p -> copre(p, mese))
                .min(Comparator.comparing(Pagamento::getDataPagamento));

        StatoQuota stato;
        if (pagamento.isPresent()) {
            stato = pagamento.get().getDataPagamento().isAfter(scadenza) ? StatoQuota.PAGATO_IN_RITARDO : StatoQuota.PAGATO_IN_TEMPO;
        } else {
            stato = oggi.isAfter(scadenza) ? StatoQuota.NON_PAGATO : StatoQuota.DA_PAGARE;
        }

        TipoAbbonamento tipo = i.getTipoAbbonamento();
        return QuotaIscrizioneDto.builder()
                .iscrizioneId(i.getId())
                .studenteId(i.getStudente().getId())
                .studenteNomeCompleto(i.getStudente().getNome() + " " + i.getStudente().getCognome())
                .corsoId(i.getCorso().getId())
                .corsoNome(i.getCorso().getNome())
                .tipoAbbonamentoNome(tipo != null ? tipo.getNome() : null)
                .quotaImporto(tipo != null ? tipo.getPrezzo() : i.getCorso().getPrezzoMensile())
                .quotaMesi(tipo != null ? tipo.mesiCoperti() : 1)
                .scadenza(scadenza)
                .stato(stato)
                .pagamentoId(pagamento.map(Pagamento::getId).orElse(null))
                .dataPagamento(pagamento.map(Pagamento::getDataPagamento).orElse(null))
                .coperturaFino(pagamento.map(p -> inizio(p).plusMonths(mesi(p) - 1L)).orElse(null))
                .build();
    }

    private LocalDate scadenza(YearMonth mese, LocalDate dataIscrizione) {
        LocalDate scadenza = mese.atDay(Math.min(giornoScadenza, mese.lengthOfMonth()));
        boolean iscrittoInQuestoMese = dataIscrizione != null && YearMonth.from(dataIscrizione).equals(mese);
        return iscrittoInQuestoMese && dataIscrizione.isAfter(scadenza) ? dataIscrizione : scadenza;
    }

    private boolean copre(Pagamento p, YearMonth mese) {
        if (p.getMeseRiferimento() == null) {
            return false;
        }
        YearMonth inizio = inizio(p);
        return !mese.isBefore(inizio) && !mese.isAfter(inizio.plusMonths(mesi(p) - 1L));
    }

    private YearMonth inizio(Pagamento p) {
        return YearMonth.from(p.getMeseRiferimento());
    }

    private int mesi(Pagamento p) {
        return p.getMesiCoperti() != null ? p.getMesiCoperti() : 1;
    }
}
