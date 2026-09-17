package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Corso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorsoRepository extends JpaRepository<Corso, Long> {
    List<Corso> findByAttivoTrue();
    List<Corso> findByIstruttoreId(Long istruttoreId);
}
