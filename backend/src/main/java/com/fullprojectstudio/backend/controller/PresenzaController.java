package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.PresenzaDto;
import com.fullprojectstudio.backend.service.PresenzaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/presenze")
@RequiredArgsConstructor
public class PresenzaController {

    private final PresenzaService presenzaService;

    @GetMapping
    public List<PresenzaDto> find(@RequestParam(required = false) Long iscrizioneId,
                                   @RequestParam(required = false) Long corsoId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        if (iscrizioneId != null) {
            return presenzaService.findByIscrizione(iscrizioneId);
        }
        if (corsoId != null && data != null) {
            return presenzaService.findByCorsoEData(corsoId, data);
        }
        return List.of();
    }

    @PostMapping
    public PresenzaDto registra(@Valid @RequestBody PresenzaDto dto) {
        return presenzaService.registra(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        presenzaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
