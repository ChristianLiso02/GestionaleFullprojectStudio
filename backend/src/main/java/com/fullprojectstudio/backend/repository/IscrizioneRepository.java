package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.StatoIscrizione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IscrizioneRepository extends JpaRepository<Iscrizione, Long> {
    List<Iscrizione> findByStudenteId(Long studenteId);
    List<Iscrizione> findByCorsoId(Long corsoId);
    List<Iscrizione> findByStato(StatoIscrizione stato);
    List<Iscrizione> findByStatoAndDataScadenzaBetween(StatoIscrizione stato, LocalDate da, LocalDate a);
    long countByCorsoIdAndStato(Long corsoId, StatoIscrizione stato);
    List<Iscrizione> findByCorso_StagioneId(Long stagioneId);
    long countByCorso_StagioneIdAndStato(Long stagioneId, StatoIscrizione stato);
}
