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

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        List<Long> istruttoriIds = dto.getIstruttoriIds() == null ? List.of() : dto.getIstruttoriIds();
        Set<Long> idsUnici = new LinkedHashSet<>(istruttoriIds);
        if (idsUnici.size() > 2) {
            throw new IllegalArgumentException("Un corso può avere al massimo 2 istruttori");
        }
        Set<Istruttore> istruttori = new LinkedHashSet<>();
        for (Long istruttoreId : idsUnici) {
            Istruttore istruttore = istruttoreRepository.findById(istruttoreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Istruttore non trovato: " + istruttoreId));
            istruttori.add(istruttore);
        }
        corso.setIstruttori(istruttori);

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
        List<Istruttore> istruttoriOrdinati = c.getIstruttori().stream()
                .sorted(Comparator.comparing(Istruttore::getCognome).thenComparing(Istruttore::getNome))
                .toList();
        return CorsoDto.builder()
                .id(c.getId())
                .nome(c.getNome())
                .stile(c.getStile())
                .livello(c.getLivello())
                .istruttoriIds(istruttoriOrdinati.stream().map(Istruttore::getId).toList())
                .istruttoriNomi(istruttoriOrdinati.stream().map(i -> i.getNome() + " " + i.getCognome()).toList())
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
