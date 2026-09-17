package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.CorsoDto;
import com.fullprojectstudio.backend.service.CorsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corsi")
@RequiredArgsConstructor
public class CorsoController {

    private final CorsoService corsoService;

    @GetMapping
    public List<CorsoDto> findAll() {
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
