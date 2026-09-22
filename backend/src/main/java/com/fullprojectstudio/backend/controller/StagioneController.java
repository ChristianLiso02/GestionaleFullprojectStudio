package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.StagioneDto;
import com.fullprojectstudio.backend.service.StagioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stagioni")
@RequiredArgsConstructor
public class StagioneController {

    private final StagioneService stagioneService;

    @GetMapping
    public List<StagioneDto> findAll() {
        return stagioneService.findAll();
    }

    @GetMapping("/{id}")
    public StagioneDto findById(@PathVariable Long id) {
        return stagioneService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StagioneDto create(@Valid @RequestBody StagioneDto dto) {
        return stagioneService.create(dto);
    }

    @PutMapping("/{id}")
    public StagioneDto update(@PathVariable Long id, @Valid @RequestBody StagioneDto dto) {
        return stagioneService.update(id, dto);
    }

    @PutMapping("/{id}/corrente")
    public StagioneDto attivaComeCorrente(@PathVariable Long id) {
        return stagioneService.attivaComeCorrente(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stagioneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
