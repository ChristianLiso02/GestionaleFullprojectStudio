package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Istruttore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IstruttoreRepository extends JpaRepository<Istruttore, Long> {
    List<Istruttore> findByAttivoTrue();
}
