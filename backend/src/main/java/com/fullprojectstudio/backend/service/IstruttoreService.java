package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.IstruttoreDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Istruttore;
import com.fullprojectstudio.backend.repository.IstruttoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IstruttoreService {

    private final IstruttoreRepository istruttoreRepository;

    public List<IstruttoreDto> findAll() {
        return istruttoreRepository.findAll().stream().map(this::toDto).toList();
    }

    public IstruttoreDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public IstruttoreDto create(IstruttoreDto dto) {
        Istruttore istruttore = fromDto(dto);
        istruttore.setId(null);
        return toDto(istruttoreRepository.save(istruttore));
    }

    public IstruttoreDto update(Long id, IstruttoreDto dto) {
        Istruttore istruttore = getEntity(id);
        istruttore.setNome(dto.getNome());
        istruttore.setCognome(dto.getCognome());
        istruttore.setTelefono(dto.getTelefono());
        istruttore.setEmail(dto.getEmail());
        istruttore.setSpecializzazioni(dto.getSpecializzazioni() == null ? new HashSet<>() : new HashSet<>(dto.getSpecializzazioni()));
        istruttore.setCompensoOrario(dto.getCompensoOrario());
        istruttore.setAttivo(dto.isAttivo());
        return toDto(istruttoreRepository.save(istruttore));
    }

    public void delete(Long id) {
        istruttoreRepository.delete(getEntity(id));
    }

    private Istruttore getEntity(Long id) {
        return istruttoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Istruttore non trovato: " + id));
    }

    private IstruttoreDto toDto(Istruttore i) {
        return IstruttoreDto.builder()
                .id(i.getId())
                .nome(i.getNome())
                .cognome(i.getCognome())
                .telefono(i.getTelefono())
                .email(i.getEmail())
                .specializzazioni(i.getSpecializzazioni())
                .compensoOrario(i.getCompensoOrario())
                .attivo(i.isAttivo())
                .build();
    }

    private Istruttore fromDto(IstruttoreDto dto) {
        return Istruttore.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .specializzazioni(dto.getSpecializzazioni() == null ? new HashSet<>() : new HashSet<>(dto.getSpecializzazioni()))
                .compensoOrario(dto.getCompensoOrario())
                .attivo(dto.isAttivo())
                .build();
    }
}
