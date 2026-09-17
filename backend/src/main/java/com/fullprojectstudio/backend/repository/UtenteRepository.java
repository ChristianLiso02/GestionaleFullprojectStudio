package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
    boolean existsByUsername(String username);
}
