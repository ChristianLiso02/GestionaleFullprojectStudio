package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Stagione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StagioneRepository extends JpaRepository<Stagione, Long> {
    Optional<Stagione> findByCorrenteTrue();
    boolean existsByNome(String nome);
}
