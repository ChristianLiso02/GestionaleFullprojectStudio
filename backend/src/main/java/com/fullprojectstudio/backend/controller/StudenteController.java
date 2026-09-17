package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.StudenteDto;
import com.fullprojectstudio.backend.service.StudenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studenti")
@RequiredArgsConstructor
public class StudenteController {

    private final StudenteService studenteService;

    @GetMapping
    public List<StudenteDto> findAll(@RequestParam(required = false) String ricerca) {
        if (ricerca != null && !ricerca.isBlank()) {
            return studenteService.ricerca(ricerca);
        }
        return studenteService.findAll();
    }

    @GetMapping("/{id}")
    public StudenteDto findById(@PathVariable Long id) {
        return studenteService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudenteDto create(@Valid @RequestBody StudenteDto dto) {
        return studenteService.create(dto);
    }

    @PutMapping("/{id}")
    public StudenteDto update(@PathVariable Long id, @Valid @RequestBody StudenteDto dto) {
        return studenteService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
