package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto;
import com.fullprojectstudio.backend.dto.QuotaIscrizioneDto.StatoQuota;
import com.fullprojectstudio.backend.dto.QuoteMeseDto;
import com.fullprojectstudio.backend.dto.QuotaScadutaDto;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.PeriodoRitiro;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calcola, per un mese, se ogni iscrizione attiva ha la quota pagata e se in tempo.
 * Regole:
 * - la quota va rinnovata entro il giorno {@code giornoScadenza} del mese: fino ad allora è "da rinnovare",
 *   dopo è "scaduta". Una quota scaduta non ritira lo studente: può pagare in ritardo;
 * - chi si iscrive o rientra dopo quel giorno rinnova il giorno stesso dell'iscrizione/rientro;
 * - chi si ritira entro la scadenza non deve la quota di quel mese, chi si ritira dopo sì;
 *   i mesi da ritirato non sono mai dovuti, per ognuno dei periodi di ritiro;
 * - un pagamento copre i mesi da meseRiferimento per mesiCoperti mesi (es. trimestrale = 3).
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

    // Mesi consecutivi scaduti dopo i quali un'iscrizione attiva va verificata (ha pagato o si è ritirata?).
    @Value("${app.pagamenti.mesi-scaduti-da-verificare:1}")
    private int mesiScadutiDaVerificare;

    private static final int MAX_MESI_A_RITROSO = 36;

    public QuoteMeseDto situazione(YearMonth mese, Long stagioneId, LocalDate oggi) {
        Long idStagione = stagioneId != null
                ? stagioneId
                : stagioneRepository.findByCorrenteTrue().map(Stagione::getId).orElse(null);
        List<QuotaIscrizioneDto> righe = idStagione == null ? List.of() : calcolaRighe(mese, idStagione, oggi);
        return QuoteMeseDto.builder().mese(mese).giornoScadenza(giornoScadenza).righe(righe).build();
    }

    /**
     * Iscrizioni attive (stagione corrente) con almeno {@code mesiScadutiDaVerificare} mesi consecutivi
     * scaduti fino ad oggi. Molti studenti smettono di venire senza avvisare: la segreteria verifica e
     * registra il pagamento oppure li segna ritirati dalla data proposta (inizio del primo mese non pagato).
     */
    public List<QuotaScadutaDto> quoteScaduteDaVerificare(LocalDate oggi) {
        Optional<Long> stagione = stagioneRepository.findByCorrenteTrue().map(Stagione::getId);
        if (stagione.isEmpty()) {
            return List.of();
        }
        List<Iscrizione> attive = iscrizioneRepository.findByCorso_StagioneId(stagione.get()).stream()
                .filter(i -> i.getStato() == StatoIscrizione.ATTIVA)
                .toList();
        Map<Long, List<Pagamento>> pagamenti = pagamentiPagati(attive);

        return attive.stream()
                .map(i -> quotaScaduta(i, oggi, pagamenti.getOrDefault(i.getId(), List.of())))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing((QuotaScadutaDto q) -> q.getMesiScaduti().size()).reversed()
                        .thenComparing(QuotaScadutaDto::getStudenteNomeCompleto, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Tutte le quote scadute di uno studente, su tutte le sue iscrizioni (anche arretrati e corsi da cui si è ritirato). */
    public List<QuotaIscrizioneDto> scaduteStudente(Long studenteId, LocalDate oggi) {
        List<QuotaIscrizioneDto> scadute = quoteAperte(iscrizioneRepository.findByStudenteId(studenteId), oggi, false);
        scadute.sort(Comparator.comparing(QuotaIscrizioneDto::getMese).thenComparing(QuotaIscrizioneDto::getCorsoNome));
        return scadute;
    }

    /** Per il backup: tutte le quote scadute della scuola più quelle del mese ancora da rinnovare, per studente. */
    public List<QuotaIscrizioneDto> quoteDaIncassare(LocalDate oggi) {
        List<QuotaIscrizioneDto> aperte = quoteAperte(iscrizioneRepository.findAll(), oggi, true);
        aperte.sort(Comparator.comparing(QuotaIscrizioneDto::getStudenteNomeCompleto, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(QuotaIscrizioneDto::getMese));
        return aperte;
    }

    private List<QuotaIscrizioneDto> quoteAperte(List<Iscrizione> iscrizioni, LocalDate oggi, boolean ancheDaRinnovare) {
        Map<Long, List<Pagamento>> pagamenti = pagamentiPagati(iscrizioni);
        YearMonth corrente = YearMonth.from(oggi);
        YearMonth limite = corrente.minusMonths(MAX_MESI_A_RITROSO - 1L);

        List<QuotaIscrizioneDto> scadute = new ArrayList<>();
        for (Iscrizione i : iscrizioni) {
            if (i.getDataIscrizione() == null) {
                continue;
            }
            List<Pagamento> pagamentiIscrizione = pagamenti.getOrDefault(i.getId(), List.of());
            YearMonth inizio = YearMonth.from(i.getDataIscrizione());
            for (YearMonth mese = inizio.isBefore(limite) ? limite : inizio; !mese.isAfter(corrente); mese = mese.plusMonths(1)) {
                if (quotaDovuta(i, mese)) {
                    QuotaIscrizioneDto riga = riga(i, mese, oggi, pagamentiIscrizione);
                    if (riga.getStato() == StatoQuota.SCADUTO || (ancheDaRinnovare && riga.getStato() == StatoQuota.DA_RINNOVARE)) {
                        scadute.add(riga);
                    }
                }
            }
        }
        return scadute;
    }

    /** Stato della quota di un mese, vuoto se in quel mese la quota non è dovuta (non ancora iscritto o ritirato). */
    public Optional<StatoQuota> statoQuotaMese(Iscrizione i, YearMonth mese, LocalDate oggi, List<Pagamento> pagamentiIscrizione) {
        if (!quotaDovuta(i, mese)) {
            return Optional.empty();
        }
        return Optional.of(statoQuota(pagamentoCheCopre(pagamentiIscrizione, mese), scadenza(mese, i), oggi));
    }

    private Optional<QuotaScadutaDto> quotaScaduta(Iscrizione i, LocalDate oggi, List<Pagamento> pagamenti) {
        List<YearMonth> mesiScaduti = mesiScadutiConsecutivi(i, oggi, pagamenti);
        if (mesiScaduti.isEmpty() || mesiScaduti.size() < mesiScadutiDaVerificare) {
            return Optional.empty();
        }
        TipoAbbonamento tipo = i.getTipoAbbonamento();
        return Optional.of(QuotaScadutaDto.builder()
                .iscrizioneId(i.getId())
                .studenteId(i.getStudente().getId())
                .studenteNomeCompleto(i.getStudente().getNome() + " " + i.getStudente().getCognome())
                .corsoId(i.getCorso().getId())
                .corsoNome(i.getCorso().getNome())
                .tipoAbbonamentoNome(tipo != null ? tipo.getNome() : null)
                .quotaImporto(tipo != null ? tipo.getPrezzo() : i.getCorso().getPrezzoMensile())
                .mesiScaduti(mesiScaduti)
                .ultimoPagamento(pagamenti.stream().map(Pagamento::getDataPagamento).max(Comparator.naturalOrder()).orElse(null))
                .dataRitiroProposta(dataRitiroProposta(i, mesiScaduti.get(0)))
                .build());
    }

    // Mesi scaduti consecutivi fino ad oggi (il mese corrente, se ancora "da rinnovare", non interrompe la serie).
    private List<YearMonth> mesiScadutiConsecutivi(Iscrizione i, LocalDate oggi, List<Pagamento> pagamenti) {
        List<YearMonth> scaduti = new ArrayList<>();
        YearMonth mese = YearMonth.from(oggi);
        for (int k = 0; k < MAX_MESI_A_RITROSO && quotaDovuta(i, mese); k++, mese = mese.minusMonths(1)) {
            StatoQuota stato = statoQuota(pagamentoCheCopre(pagamenti, mese), scadenza(mese, i), oggi);
            if (stato == StatoQuota.DA_RINNOVARE && k == 0) {
                continue;
            }
            if (stato != StatoQuota.SCADUTO) {
                break;
            }
            scaduti.add(0, mese);
        }
        return scaduti;
    }

    // Inizio del primo mese non pagato, ma mai prima dell'iscrizione o dell'ultimo rientro (non sarebbe accettata).
    private LocalDate dataRitiroProposta(Iscrizione i, YearMonth primoMeseScaduto) {
        Stream<LocalDate> limiti = Stream.concat(
                Stream.ofNullable(i.getDataIscrizione()),
                i.getRitiri().stream().map(PeriodoRitiro::getDataRientro).filter(Objects::nonNull));
        return Stream.concat(Stream.of(primoMeseScaduto.atDay(1)), limiti)
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    private Map<Long, List<Pagamento>> pagamentiPagati(List<Iscrizione> iscrizioni) {
        if (iscrizioni.isEmpty()) {
            return Map.of();
        }
        return pagamentoRepository
                .findByIscrizioneIdInAndStato(iscrizioni.stream().map(Iscrizione::getId).toList(), StatoPagamento.PAGATO)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getIscrizione().getId()));
    }

    private List<QuotaIscrizioneDto> calcolaRighe(YearMonth mese, Long stagioneId, LocalDate oggi) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByCorso_StagioneId(stagioneId).stream()
                .filter(i -> quotaDovuta(i, mese))
                .toList();
        Map<Long, List<Pagamento>> pagamentiPerIscrizione = pagamentiPagati(iscrizioni);

        return iscrizioni.stream()
                .map(i -> riga(i, mese, oggi, pagamentiPerIscrizione.getOrDefault(i.getId(), List.of())))
                .sorted(Comparator.comparing(QuotaIscrizioneDto::getStato)
                        .thenComparing(QuotaIscrizioneDto::getStudenteNomeCompleto, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private QuotaIscrizioneDto riga(Iscrizione i, YearMonth mese, LocalDate oggi, List<Pagamento> pagamenti) {
        LocalDate scadenza = scadenza(mese, i);
        Optional<Pagamento> pagamento = pagamentoCheCopre(pagamenti, mese);
        StatoQuota stato = statoQuota(pagamento, scadenza, oggi);
        // Proposta di ritiro solo se da quel mese in poi non ha più pagato: chi paga i mesi successivi sta frequentando.
        LocalDate dataRitiroProposta = null;
        if (stato == StatoQuota.SCADUTO && i.getStato() == StatoIscrizione.ATTIVA) {
            List<YearMonth> scaduti = mesiScadutiConsecutivi(i, oggi, pagamenti);
            if (scaduti.contains(mese)) {
                dataRitiroProposta = dataRitiroProposta(i, scaduti.get(0));
            }
        }

        TipoAbbonamento tipo = i.getTipoAbbonamento();
        return QuotaIscrizioneDto.builder()
                .mese(mese)
                .iscrizioneId(i.getId())
                .studenteId(i.getStudente().getId())
                .studenteNomeCompleto(i.getStudente().getNome() + " " + i.getStudente().getCognome())
                .corsoId(i.getCorso().getId())
                .corsoNome(i.getCorso().getNome())
                .statoIscrizione(i.getStato())
                .tipoAbbonamentoNome(tipo != null ? tipo.getNome() : null)
                .quotaImporto(tipo != null ? tipo.getPrezzo() : i.getCorso().getPrezzoMensile())
                .quotaMesi(tipo != null ? tipo.mesiCoperti() : 1)
                .scadenza(scadenza)
                .stato(stato)
                .pagamentoId(pagamento.map(Pagamento::getId).orElse(null))
                .dataPagamento(pagamento.map(Pagamento::getDataPagamento).orElse(null))
                .coperturaFino(pagamento.map(p -> inizio(p).plusMonths(mesi(p) - 1L)).orElse(null))
                .dataRitiroProposta(dataRitiroProposta)
                .build();
    }

    private Optional<Pagamento> pagamentoCheCopre(List<Pagamento> pagamenti, YearMonth mese) {
        return pagamenti.stream()
                .filter(p -> copre(p, mese))
                .min(Comparator.comparing(Pagamento::getDataPagamento));
    }

    private StatoQuota statoQuota(Optional<Pagamento> pagamento, LocalDate scadenza, LocalDate oggi) {
        if (pagamento.isPresent()) {
            return pagamento.get().getDataPagamento().isAfter(scadenza) ? StatoQuota.PAGATO_IN_RITARDO : StatoQuota.PAGATO_IN_TEMPO;
        }
        return oggi.isAfter(scadenza) ? StatoQuota.SCADUTO : StatoQuota.DA_RINNOVARE;
    }

    private boolean quotaDovuta(Iscrizione i, YearMonth mese) {
        if (i.getDataIscrizione() != null && mese.isBefore(YearMonth.from(i.getDataIscrizione()))) {
            return false;
        }
        // Ritirata prima che esistesse lo storico dei ritiri: nessuna quota dovuta.
        if (i.getStato() == StatoIscrizione.RITIRATO && i.ritiroInCorso().isEmpty()) {
            return false;
        }
        return i.getRitiri().stream().noneMatch(p -> sospeso(p, mese));
    }

    private boolean sospeso(PeriodoRitiro p, YearMonth mese) {
        return !mese.isBefore(primoMeseNonDovuto(p.getDataRitiro()))
                && (p.getDataRientro() == null || mese.isBefore(YearMonth.from(p.getDataRientro())));
    }

    private YearMonth primoMeseNonDovuto(LocalDate dataRitiro) {
        YearMonth meseRitiro = YearMonth.from(dataRitiro);
        return dataRitiro.isAfter(scadenzaBase(meseRitiro)) ? meseRitiro.plusMonths(1) : meseRitiro;
    }

    private LocalDate scadenzaBase(YearMonth mese) {
        return mese.atDay(Math.min(giornoScadenza, mese.lengthOfMonth()));
    }

    // Nel mese in cui si inizia (iscrizione o rientro dopo un ritiro) si rinnova quel giorno, se è dopo la scadenza normale.
    private LocalDate scadenza(YearMonth mese, Iscrizione i) {
        Stream<LocalDate> iscrizione = Stream.ofNullable(i.getDataIscrizione());
        Stream<LocalDate> rientri = i.getRitiri().stream()
                .filter(p -> p.getDataRientro() != null && !mese.isBefore(primoMeseNonDovuto(p.getDataRitiro())))
                .map(PeriodoRitiro::getDataRientro);
        return Stream.concat(iscrizione, rientri)
                .filter(inizio -> YearMonth.from(inizio).equals(mese))
                .max(Comparator.naturalOrder())
                .filter(inizio -> inizio.isAfter(scadenzaBase(mese)))
                .orElse(scadenzaBase(mese));
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
