package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.TipoAbbonamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoAbbonamentoRepository extends JpaRepository<TipoAbbonamento, Long> {
    List<TipoAbbonamento> findByAttivoTrue();
}
