package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.SalaDto;
import com.fullprojectstudio.backend.service.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sale")
@RequiredArgsConstructor
public class SalaController {

    private final SalaService salaService;

    @GetMapping
    public List<SalaDto> findAll() {
        return salaService.findAll();
    }

    @GetMapping("/{id}")
    public SalaDto findById(@PathVariable Long id) {
        return salaService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaDto create(@Valid @RequestBody SalaDto dto) {
        return salaService.create(dto);
    }

    @PutMapping("/{id}")
    public SalaDto update(@PathVariable Long id, @Valid @RequestBody SalaDto dto) {
        return salaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
