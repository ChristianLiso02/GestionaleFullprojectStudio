package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.TipoAbbonamentoDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.TipoAbbonamento;
import com.fullprojectstudio.backend.repository.TipoAbbonamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoAbbonamentoService {

    private final TipoAbbonamentoRepository tipoAbbonamentoRepository;

    public List<TipoAbbonamentoDto> findAll() {
        return tipoAbbonamentoRepository.findAll().stream().map(this::toDto).toList();
    }

    public TipoAbbonamentoDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public TipoAbbonamentoDto create(TipoAbbonamentoDto dto) {
        TipoAbbonamento tipo = TipoAbbonamento.builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .durataGiorni(dto.getDurataGiorni())
                .numeroLezioni(dto.getNumeroLezioni())
                .prezzo(dto.getPrezzo())
                .attivo(dto.isAttivo())
                .build();
        return toDto(tipoAbbonamentoRepository.save(tipo));
    }

    public TipoAbbonamentoDto update(Long id, TipoAbbonamentoDto dto) {
        TipoAbbonamento tipo = getEntity(id);
        tipo.setNome(dto.getNome());
        tipo.setDescrizione(dto.getDescrizione());
        tipo.setDurataGiorni(dto.getDurataGiorni());
        tipo.setNumeroLezioni(dto.getNumeroLezioni());
        tipo.setPrezzo(dto.getPrezzo());
        tipo.setAttivo(dto.isAttivo());
        return toDto(tipoAbbonamentoRepository.save(tipo));
    }

    public void delete(Long id) {
        tipoAbbonamentoRepository.delete(getEntity(id));
    }

    private TipoAbbonamento getEntity(Long id) {
        return tipoAbbonamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo abbonamento non trovato: " + id));
    }

    private TipoAbbonamentoDto toDto(TipoAbbonamento t) {
        return TipoAbbonamentoDto.builder()
                .id(t.getId())
                .nome(t.getNome())
                .descrizione(t.getDescrizione())
                .durataGiorni(t.getDurataGiorni())
                .numeroLezioni(t.getNumeroLezioni())
                .prezzo(t.getPrezzo())
                .attivo(t.isAttivo())
                .build();
    }
}
