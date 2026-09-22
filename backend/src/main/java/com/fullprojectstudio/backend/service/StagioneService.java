package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.StagioneDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Stagione;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StagioneService {

    private final StagioneRepository stagioneRepository;

    public List<StagioneDto> findAll() {
        return stagioneRepository.findAll().stream()
                .sorted(Comparator.comparing(Stagione::getNome).reversed())
                .map(this::toDto)
                .toList();
    }

    public StagioneDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public StagioneDto create(StagioneDto dto) {
        if (stagioneRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Esiste già una stagione con questo nome");
        }
        Stagione stagione = Stagione.builder()
                .nome(dto.getNome())
                .dataInizio(dto.getDataInizio())
                .dataFine(dto.getDataFine())
                .corrente(false)
                .build();
        stagione = stagioneRepository.save(stagione);

        if (dto.isCorrente() || stagioneRepository.findByCorrenteTrue().isEmpty()) {
            impostaComeCorrente(stagione);
            stagione = stagioneRepository.save(stagione);
        }
        return toDto(stagione);
    }

    public StagioneDto update(Long id, StagioneDto dto) {
        Stagione stagione = getEntity(id);
        if (!stagione.getNome().equals(dto.getNome()) && stagioneRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Esiste già una stagione con questo nome");
        }
        stagione.setNome(dto.getNome());
        stagione.setDataInizio(dto.getDataInizio());
        stagione.setDataFine(dto.getDataFine());
        return toDto(stagioneRepository.save(stagione));
    }

    public StagioneDto attivaComeCorrente(Long id) {
        Stagione stagione = getEntity(id);
        impostaComeCorrente(stagione);
        return toDto(stagioneRepository.save(stagione));
    }

    public void delete(Long id) {
        Stagione stagione = getEntity(id);
        if (stagione.isCorrente()) {
            throw new IllegalArgumentException("Non puoi eliminare la stagione corrente. Rendi prima un'altra stagione come corrente.");
        }
        stagioneRepository.delete(stagione);
    }

    private void impostaComeCorrente(Stagione stagione) {
        stagioneRepository.findByCorrenteTrue().ifPresent(precedente -> {
            if (!precedente.getId().equals(stagione.getId())) {
                precedente.setCorrente(false);
                stagioneRepository.save(precedente);
            }
        });
        stagione.setCorrente(true);
    }

    private Stagione getEntity(Long id) {
        return stagioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stagione non trovata: " + id));
    }

    private StagioneDto toDto(Stagione s) {
        return StagioneDto.builder()
                .id(s.getId())
                .nome(s.getNome())
                .dataInizio(s.getDataInizio())
                .dataFine(s.getDataFine())
                .corrente(s.isCorrente())
                .build();
    }
}
