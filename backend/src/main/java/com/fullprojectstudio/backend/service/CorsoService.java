package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.CorsoDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Corso;
import com.fullprojectstudio.backend.model.Istruttore;
import com.fullprojectstudio.backend.model.Sala;
import com.fullprojectstudio.backend.repository.CorsoRepository;
import com.fullprojectstudio.backend.repository.IstruttoreRepository;
import com.fullprojectstudio.backend.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CorsoService {

    private final CorsoRepository corsoRepository;
    private final IstruttoreRepository istruttoreRepository;
    private final SalaRepository salaRepository;

    public List<CorsoDto> findAll() {
        return corsoRepository.findAll().stream().map(this::toDto).toList();
    }

    public CorsoDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public CorsoDto create(CorsoDto dto) {
        Corso corso = new Corso();
        applyDto(corso, dto);
        return toDto(corsoRepository.save(corso));
    }

    public CorsoDto update(Long id, CorsoDto dto) {
        Corso corso = getEntity(id);
        applyDto(corso, dto);
        return toDto(corsoRepository.save(corso));
    }

    public void delete(Long id) {
        corsoRepository.delete(getEntity(id));
    }

    private void applyDto(Corso corso, CorsoDto dto) {
        corso.setNome(dto.getNome());
        corso.setStile(dto.getStile());
        corso.setLivello(dto.getLivello());
        corso.setGiorniSettimana(dto.getGiorniSettimana() == null ? new HashSet<>() : new HashSet<>(dto.getGiorniSettimana()));
        corso.setOrarioInizio(dto.getOrarioInizio());
        corso.setOrarioFine(dto.getOrarioFine());
        corso.setCapienzaMax(dto.getCapienzaMax());
        corso.setPrezzoMensile(dto.getPrezzoMensile());
        corso.setDataInizio(dto.getDataInizio());
        corso.setDataFine(dto.getDataFine());
        corso.setAttivo(dto.isAttivo());

        if (dto.getIstruttoreId() != null) {
            Istruttore istruttore = istruttoreRepository.findById(dto.getIstruttoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Istruttore non trovato: " + dto.getIstruttoreId()));
            corso.setIstruttore(istruttore);
        } else {
            corso.setIstruttore(null);
        }

        if (dto.getSalaId() != null) {
            Sala sala = salaRepository.findById(dto.getSalaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sala non trovata: " + dto.getSalaId()));
            corso.setSala(sala);
        } else {
            corso.setSala(null);
        }
    }

    private Corso getEntity(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato: " + id));
    }

    private CorsoDto toDto(Corso c) {
        return CorsoDto.builder()
                .id(c.getId())
                .nome(c.getNome())
                .stile(c.getStile())
                .livello(c.getLivello())
                .istruttoreId(c.getIstruttore() != null ? c.getIstruttore().getId() : null)
                .istruttoreNome(c.getIstruttore() != null ? c.getIstruttore().getNome() + " " + c.getIstruttore().getCognome() : null)
                .salaId(c.getSala() != null ? c.getSala().getId() : null)
                .salaNome(c.getSala() != null ? c.getSala().getNome() : null)
                .giorniSettimana(c.getGiorniSettimana())
                .orarioInizio(c.getOrarioInizio())
                .orarioFine(c.getOrarioFine())
                .capienzaMax(c.getCapienzaMax())
                .prezzoMensile(c.getPrezzoMensile())
                .dataInizio(c.getDataInizio())
                .dataFine(c.getDataFine())
                .attivo(c.isAttivo())
                .build();
    }
}
