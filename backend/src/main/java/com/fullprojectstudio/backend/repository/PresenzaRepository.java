package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenzaRepository extends JpaRepository<Presenza, Long> {
    List<Presenza> findByIscrizioneId(Long iscrizioneId);
    List<Presenza> findByIscrizioneCorsoIdAndDataLezione(Long corsoId, LocalDate dataLezione);
    Optional<Presenza> findByIscrizioneIdAndDataLezione(Long iscrizioneId, LocalDate dataLezione);
}
