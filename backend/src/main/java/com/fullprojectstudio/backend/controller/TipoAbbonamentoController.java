package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.TipoAbbonamentoDto;
import com.fullprojectstudio.backend.service.TipoAbbonamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abbonamenti")
@RequiredArgsConstructor
public class TipoAbbonamentoController {

    private final TipoAbbonamentoService tipoAbbonamentoService;

    @GetMapping
    public List<TipoAbbonamentoDto> findAll() {
        return tipoAbbonamentoService.findAll();
    }

    @GetMapping("/{id}")
    public TipoAbbonamentoDto findById(@PathVariable Long id) {
        return tipoAbbonamentoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoAbbonamentoDto create(@Valid @RequestBody TipoAbbonamentoDto dto) {
        return tipoAbbonamentoService.create(dto);
    }

    @PutMapping("/{id}")
    public TipoAbbonamentoDto update(@PathVariable Long id, @Valid @RequestBody TipoAbbonamentoDto dto) {
        return tipoAbbonamentoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tipoAbbonamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
