package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Studente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudenteRepository extends JpaRepository<Studente, Long> {
    List<Studente> findByAttivoTrue();
    List<Studente> findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(String nome, String cognome);
}
