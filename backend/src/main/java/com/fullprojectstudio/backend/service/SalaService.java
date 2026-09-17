package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.SalaDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Sala;
import com.fullprojectstudio.backend.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaService {

    private final SalaRepository salaRepository;

    public List<SalaDto> findAll() {
        return salaRepository.findAll().stream().map(this::toDto).toList();
    }

    public SalaDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public SalaDto create(SalaDto dto) {
        Sala sala = Sala.builder().nome(dto.getNome()).capienza(dto.getCapienza()).note(dto.getNote()).build();
        return toDto(salaRepository.save(sala));
    }

    public SalaDto update(Long id, SalaDto dto) {
        Sala sala = getEntity(id);
        sala.setNome(dto.getNome());
        sala.setCapienza(dto.getCapienza());
        sala.setNote(dto.getNote());
        return toDto(salaRepository.save(sala));
    }

    public void delete(Long id) {
        salaRepository.delete(getEntity(id));
    }

    private Sala getEntity(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala non trovata: " + id));
    }

    private SalaDto toDto(Sala s) {
        return SalaDto.builder().id(s.getId()).nome(s.getNome()).capienza(s.getCapienza()).note(s.getNote()).build();
    }
}
