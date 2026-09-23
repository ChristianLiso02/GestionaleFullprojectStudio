package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.IscrizioneDto;
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

    public List<IscrizioneDto> findInScadenza(int giorni) {
        LocalDate oggi = LocalDate.now();
        return iscrizioneRepository.findByStatoAndDataScadenzaBetween(StatoIscrizione.ATTIVA, oggi, oggi.plusDays(giorni))
                .stream().map(this::toDto).toList();
    }

    public IscrizioneDto create(IscrizioneDto dto) {
        Iscrizione iscrizione = new Iscrizione();
        applyDto(iscrizione, dto);
        if (iscrizione.getDataIscrizione() == null) {
            iscrizione.setDataIscrizione(LocalDate.now());
        }
        if (iscrizione.getDataScadenza() == null && iscrizione.getTipoAbbonamento() != null
                && iscrizione.getTipoAbbonamento().getDurataGiorni() != null) {
            iscrizione.setDataScadenza(iscrizione.getDataIscrizione().plusDays(iscrizione.getTipoAbbonamento().getDurataGiorni()));
        }
        return toDto(iscrizioneRepository.save(iscrizione));
    }

    public IscrizioneDto update(Long id, IscrizioneDto dto) {
        Iscrizione iscrizione = getEntity(id);
        applyDto(iscrizione, dto);
        return toDto(iscrizioneRepository.save(iscrizione));
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
        StatoIscrizione statoNuovo = dto.getStato() != null ? dto.getStato() : StatoIscrizione.ATTIVA;
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
        iscrizione.setDataScadenza(dto.getDataScadenza());
        iscrizione.setStato(dto.getStato() != null ? dto.getStato() : StatoIscrizione.ATTIVA);
        iscrizione.setNote(dto.getNote());
    }

    private void verificaCapienza(Corso corso, StatoIscrizione statoNuovo, StatoIscrizione statoPrecedente, Long corsoPrecedenteId) {
        if (statoNuovo != StatoIscrizione.ATTIVA || corso.getCapienzaMax() == null) return;

        boolean giaOccupavaUnPosto = statoPrecedente == StatoIscrizione.ATTIVA && corso.getId().equals(corsoPrecedenteId);
        if (giaOccupavaUnPosto) return;

        long attivi = iscrizioneRepository.countByCorsoIdAndStato(corso.getId(), StatoIscrizione.ATTIVA);
        if (attivi >= corso.getCapienzaMax()) {
            throw new IllegalArgumentException("Capienza massima del corso raggiunta (" + corso.getCapienzaMax()
                    + " iscritti attivi). Segna un'iscrizione come scaduta o annullata prima di aggiungerne un'altra.");
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
                .dataScadenza(i.getDataScadenza())
                .stato(i.getStato())
                .note(i.getNote())
                .build();
    }
}
