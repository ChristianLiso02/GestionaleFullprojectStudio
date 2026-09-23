package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.PagamentoDto;
import com.fullprojectstudio.backend.dto.QuoteMeseDto;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.service.PagamentoService;
import com.fullprojectstudio.backend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final QuoteService quoteService;

    @GetMapping("/quote")
    public QuoteMeseDto quote(@RequestParam YearMonth mese, @RequestParam(required = false) Long stagioneId) {
        return quoteService.situazione(mese, stagioneId, LocalDate.now());
    }

    @GetMapping
    public List<PagamentoDto> findAll(@RequestParam(required = false) Long studenteId,
                                       @RequestParam(required = false) StatoPagamento stato) {
        if (studenteId != null) {
            return pagamentoService.findByStudente(studenteId);
        }
        if (stato != null) {
            return pagamentoService.findByStato(stato);
        }
        return pagamentoService.findAll();
    }

    @GetMapping("/{id}")
    public PagamentoDto findById(@PathVariable Long id) {
        return pagamentoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoDto create(@Valid @RequestBody PagamentoDto dto) {
        return pagamentoService.create(dto);
    }

    @PutMapping("/{id}")
    public PagamentoDto update(@PathVariable Long id, @Valid @RequestBody PagamentoDto dto) {
        return pagamentoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
