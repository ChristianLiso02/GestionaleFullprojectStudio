package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.CorsoDto;
import com.fullprojectstudio.backend.service.CorsoService;
import com.fullprojectstudio.backend.service.EsportazioneCorsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/corsi")
@RequiredArgsConstructor
public class CorsoController {

    private final CorsoService corsoService;
    private final EsportazioneCorsoService esportazioneCorsoService;

    @GetMapping("/{id}/esporta")
    public ResponseEntity<byte[]> esporta(@PathVariable Long id) {
        EsportazioneCorsoService.FileEsportato file = esportazioneCorsoService.esporta(id, LocalDate.now());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.nome(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file.contenuto());
    }

    @GetMapping
    public List<CorsoDto> findAll(@RequestParam(required = false) Long stagioneId) {
        if (stagioneId != null) {
            return corsoService.findByStagione(stagioneId);
        }
        return corsoService.findAll();
    }

    @GetMapping("/{id}")
    public CorsoDto findById(@PathVariable Long id) {
        return corsoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorsoDto create(@Valid @RequestBody CorsoDto dto) {
        return corsoService.create(dto);
    }

    @PutMapping("/{id}")
    public CorsoDto update(@PathVariable Long id, @Valid @RequestBody CorsoDto dto) {
        return corsoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        corsoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
