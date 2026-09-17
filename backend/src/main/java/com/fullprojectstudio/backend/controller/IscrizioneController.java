package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.IscrizioneDto;
import com.fullprojectstudio.backend.service.IscrizioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iscrizioni")
@RequiredArgsConstructor
public class IscrizioneController {

    private final IscrizioneService iscrizioneService;

    @GetMapping
    public List<IscrizioneDto> findAll(@RequestParam(required = false) Long studenteId,
                                        @RequestParam(required = false) Long corsoId,
                                        @RequestParam(required = false) Integer inScadenzaGiorni) {
        if (studenteId != null) {
            return iscrizioneService.findByStudente(studenteId);
        }
        if (corsoId != null) {
            return iscrizioneService.findByCorso(corsoId);
        }
        if (inScadenzaGiorni != null) {
            return iscrizioneService.findInScadenza(inScadenzaGiorni);
        }
        return iscrizioneService.findAll();
    }

    @GetMapping("/{id}")
    public IscrizioneDto findById(@PathVariable Long id) {
        return iscrizioneService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IscrizioneDto create(@Valid @RequestBody IscrizioneDto dto) {
        return iscrizioneService.create(dto);
    }

    @PutMapping("/{id}")
    public IscrizioneDto update(@PathVariable Long id, @Valid @RequestBody IscrizioneDto dto) {
        return iscrizioneService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        iscrizioneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
