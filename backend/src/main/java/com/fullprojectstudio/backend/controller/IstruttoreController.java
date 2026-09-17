package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.IstruttoreDto;
import com.fullprojectstudio.backend.service.IstruttoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/istruttori")
@RequiredArgsConstructor
public class IstruttoreController {

    private final IstruttoreService istruttoreService;

    @GetMapping
    public List<IstruttoreDto> findAll() {
        return istruttoreService.findAll();
    }

    @GetMapping("/{id}")
    public IstruttoreDto findById(@PathVariable Long id) {
        return istruttoreService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IstruttoreDto create(@Valid @RequestBody IstruttoreDto dto) {
        return istruttoreService.create(dto);
    }

    @PutMapping("/{id}")
    public IstruttoreDto update(@PathVariable Long id, @Valid @RequestBody IstruttoreDto dto) {
        return istruttoreService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        istruttoreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
