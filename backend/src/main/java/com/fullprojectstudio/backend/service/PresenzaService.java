package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.PresenzaDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Presenza;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PresenzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenzaService {

    private final PresenzaRepository presenzaRepository;
    private final IscrizioneRepository iscrizioneRepository;

    public List<PresenzaDto> findByIscrizione(Long iscrizioneId) {
        return presenzaRepository.findByIscrizioneId(iscrizioneId).stream().map(this::toDto).toList();
    }

    public List<PresenzaDto> findByCorsoEData(Long corsoId, LocalDate data) {
        return presenzaRepository.findByIscrizioneCorsoIdAndDataLezione(corsoId, data).stream().map(this::toDto).toList();
    }

    public PresenzaDto registra(PresenzaDto dto) {
        Iscrizione iscrizione = iscrizioneRepository.findById(dto.getIscrizioneId())
                .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata: " + dto.getIscrizioneId()));

        Presenza presenza = presenzaRepository.findByIscrizioneIdAndDataLezione(dto.getIscrizioneId(), dto.getDataLezione())
                .orElseGet(() -> Presenza.builder().iscrizione(iscrizione).dataLezione(dto.getDataLezione()).build());

        presenza.setPresente(dto.isPresente());
        presenza.setNote(dto.getNote());

        return toDto(presenzaRepository.save(presenza));
    }

    public void delete(Long id) {
        Presenza presenza = presenzaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presenza non trovata: " + id));
        presenzaRepository.delete(presenza);
    }

    private PresenzaDto toDto(Presenza p) {
        return PresenzaDto.builder()
                .id(p.getId())
                .iscrizioneId(p.getIscrizione().getId())
                .studenteNomeCompleto(p.getIscrizione().getStudente().getNome() + " " + p.getIscrizione().getStudente().getCognome())
                .dataLezione(p.getDataLezione())
                .presente(p.isPresente())
                .note(p.getNote())
                .build();
    }
}
