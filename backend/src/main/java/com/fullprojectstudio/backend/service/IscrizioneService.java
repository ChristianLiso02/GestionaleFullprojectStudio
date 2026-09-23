package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.IscrizioneDto;
import com.fullprojectstudio.backend.dto.PeriodoRitiroDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.*;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import com.fullprojectstudio.backend.repository.TipoAbbonamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IscrizioneService {

    private final IscrizioneRepository iscrizioneRepository;
    private final StudenteRepository studenteRepository;
    private final CorsoRepository corsoRepository;
    private final TipoAbbonamentoRepository tipoAbbonamentoRepository;

    public List<IscrizioneDto> findAll() {
        return iscrizioneRepository.findAll().stream().map(this::toDto).toList();
    }

    public IscrizioneDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public List<IscrizioneDto> findByStudente(Long studenteId) {
        return iscrizioneRepository.findByStudenteId(studenteId).stream().map(this::toDto).toList();
    }

    public List<IscrizioneDto> findByCorso(Long corsoId) {
        return iscrizioneRepository.findByCorsoId(corsoId).stream().map(this::toDto).toList();
    }

    public List<IscrizioneDto> findByStagione(Long stagioneId) {
        return iscrizioneRepository.findByCorso_StagioneId(stagioneId).stream().map(this::toDto).toList();
    }

    public IscrizioneDto create(IscrizioneDto dto) {
        Iscrizione iscrizione = new Iscrizione();
        applyDto(iscrizione, dto);
        if (iscrizione.getDataIscrizione() == null) {
            iscrizione.setDataIscrizione(LocalDate.now());
        }
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    public IscrizioneDto update(Long id, IscrizioneDto dto) {
        Iscrizione iscrizione = getEntity(id);
        applyDto(iscrizione, dto);
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    public IscrizioneDto ritira(Long id, LocalDate data) {
        Iscrizione iscrizione = getEntity(id);
        if (iscrizione.getStato() == StatoIscrizione.RITIRATO) {
            throw new IllegalArgumentException("Lo studente risulta già ritirato da questo corso.");
        }
        LocalDate dataRitiro = data != null ? data : LocalDate.now();
        verificaNonFutura(dataRitiro);
        if (iscrizione.getDataIscrizione() != null && dataRitiro.isBefore(iscrizione.getDataIscrizione())) {
            throw new IllegalArgumentException("La data di ritiro non può essere precedente all'iscrizione ("
                    + iscrizione.getDataIscrizione() + ").");
        }
        iscrizione.getRitiri().stream()
                .map(PeriodoRitiro::getDataRientro)
                .filter(rientro -> rientro != null && dataRitiro.isBefore(rientro))
                .findAny()
                .ifPresent(rientro -> {
                    throw new IllegalArgumentException("La data di ritiro non può essere precedente all'ultimo rientro (" + rientro + ").");
                });

        iscrizione.getRitiri().add(PeriodoRitiro.builder().dataRitiro(dataRitiro).build());
        iscrizione.setStato(StatoIscrizione.RITIRATO);
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    public IscrizioneDto riattiva(Long id, LocalDate data) {
        Iscrizione iscrizione = getEntity(id);
        if (iscrizione.getStato() == StatoIscrizione.ATTIVA) {
            throw new IllegalArgumentException("Lo studente risulta già attivo in questo corso.");
        }
        LocalDate dataRientro = data != null ? data : LocalDate.now();
        verificaNonFutura(dataRientro);
        verificaCapienza(iscrizione.getCorso(), StatoIscrizione.ATTIVA, iscrizione.getStato(), iscrizione.getCorso().getId());

        // Iscrizioni ritirate prima dello storico dei ritiri: si considera ritirata dall'iscrizione.
        PeriodoRitiro periodo = iscrizione.ritiroInCorso().orElseGet(() -> {
            PeriodoRitiro nuovo = PeriodoRitiro.builder().dataRitiro(iscrizione.getDataIscrizione()).build();
            iscrizione.getRitiri().add(nuovo);
            return nuovo;
        });
        if (dataRientro.isBefore(periodo.getDataRitiro())) {
            throw new IllegalArgumentException("La data di rientro non può essere precedente al ritiro (" + periodo.getDataRitiro() + ").");
        }
        periodo.setDataRientro(dataRientro);
        iscrizione.setStato(StatoIscrizione.ATTIVA);
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    // Per correggere un ritiro inserito per errore: i mesi che copriva tornano dovuti.
    public IscrizioneDto annullaRitiro(Long id, Long periodoId) {
        Iscrizione iscrizione = getEntity(id);
        PeriodoRitiro periodo = iscrizione.getRitiri().stream()
                .filter(p -> p.getId().equals(periodoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Periodo di ritiro non trovato: " + periodoId));
        if (periodo.getDataRientro() == null) {
            verificaCapienza(iscrizione.getCorso(), StatoIscrizione.ATTIVA, iscrizione.getStato(), iscrizione.getCorso().getId());
            iscrizione.setStato(StatoIscrizione.ATTIVA);
        }
        iscrizione.getRitiri().remove(periodo);
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    private void verificaNonFutura(LocalDate data) {
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La data non può essere nel futuro.");
        }
    }

    public void delete(Long id) {
        iscrizioneRepository.delete(getEntity(id));
    }

    private void applyDto(Iscrizione iscrizione, IscrizioneDto dto) {
        Studente studente = studenteRepository.findById(dto.getStudenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + dto.getStudenteId()));
        Corso corso = corsoRepository.findById(dto.getCorsoId())
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato: " + dto.getCorsoId()));

        StatoIscrizione statoPrecedente = iscrizione.getStato();
        Long corsoPrecedenteId = iscrizione.getCorso() != null ? iscrizione.getCorso().getId() : null;
        StatoIscrizione statoNuovo = statoPrecedente != null ? statoPrecedente : StatoIscrizione.ATTIVA;
        verificaCapienza(corso, statoNuovo, statoPrecedente, corsoPrecedenteId);

        iscrizione.setStudente(studente);
        iscrizione.setCorso(corso);

        if (dto.getTipoAbbonamentoId() != null) {
            TipoAbbonamento tipo = tipoAbbonamentoRepository.findById(dto.getTipoAbbonamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo abbonamento non trovato: " + dto.getTipoAbbonamentoId()));
            iscrizione.setTipoAbbonamento(tipo);
        } else {
            iscrizione.setTipoAbbonamento(null);
        }

        if (dto.getDataIscrizione() != null) {
            iscrizione.setDataIscrizione(dto.getDataIscrizione());
        }
        iscrizione.setStato(statoNuovo);
        iscrizione.setNote(dto.getNote());
    }

    private void verificaCapienza(Corso corso, StatoIscrizione statoNuovo, StatoIscrizione statoPrecedente, Long corsoPrecedenteId) {
        if (statoNuovo != StatoIscrizione.ATTIVA || corso.getCapienzaMax() == null) return;

        boolean giaOccupavaUnPosto = statoPrecedente == StatoIscrizione.ATTIVA && corso.getId().equals(corsoPrecedenteId);
        if (giaOccupavaUnPosto) return;

        long attivi = iscrizioneRepository.countByCorsoIdAndStato(corso.getId(), StatoIscrizione.ATTIVA);
        if (attivi >= corso.getCapienzaMax()) {
            throw new IllegalArgumentException("Capienza massima del corso raggiunta (" + corso.getCapienzaMax()
                    + " iscritti attivi). Segna come ritirato chi ha lasciato il corso prima di aggiungere o riattivare un iscritto.");
        }
    }

    private Iscrizione getEntity(Long id) {
        return iscrizioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata: " + id));
    }

    private IscrizioneDto toDto(Iscrizione i) {
        return IscrizioneDto.builder()
                .id(i.getId())
                .studenteId(i.getStudente().getId())
                .studenteNomeCompleto(i.getStudente().getNome() + " " + i.getStudente().getCognome())
                .corsoId(i.getCorso().getId())
                .corsoNome(i.getCorso().getNome())
                .stagioneNome(i.getCorso().getStagione() != null ? i.getCorso().getStagione().getNome() : null)
                .tipoAbbonamentoId(i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().getId() : null)
                .tipoAbbonamentoNome(i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().getNome() : null)
                .quotaImporto(i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().getPrezzo() : i.getCorso().getPrezzoMensile())
                .quotaMesi(i.getTipoAbbonamento() != null ? i.getTipoAbbonamento().mesiCoperti() : 1)
                .dataIscrizione(i.getDataIscrizione())
                .dataRitiro(i.ritiroInCorso().map(PeriodoRitiro::getDataRitiro).orElse(null))
                .ritiri(i.getRitiri().stream()
                        .map(p -> PeriodoRitiroDto.builder().id(p.getId()).dataRitiro(p.getDataRitiro()).dataRientro(p.getDataRientro()).build())
                        .toList())
                .stato(i.getStato())
                .note(i.getNote())
                .build();
    }
}
