package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.StagioneDto;
import com.fullprojectstudio.backend.model.Stagione;
import com.fullprojectstudio.backend.repository.StagioneRepository;
import com.fullprojectstudio.backend.service.StagioneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica l'invariante "una sola stagione corrente alla volta" e la
 * protezione contro l'eliminazione della stagione corrente.
 */
@ExtendWith(MockitoExtension.class)
class StagioneServiceTest {

    @Mock private StagioneRepository stagioneRepository;

    @InjectMocks private StagioneService stagioneService;

    @Test
    void impostareUnaNuovaStagioneComeCorrenteTogliIlFlagAllaPrecedente() {
        Stagione precedente = Stagione.builder().id(1L).nome("2026/2027").corrente(true).build();

        when(stagioneRepository.existsByNome("2027/2028")).thenReturn(false);
        when(stagioneRepository.save(any(Stagione.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stagioneRepository.findByCorrenteTrue()).thenReturn(Optional.of(precedente));

        StagioneDto risultato = stagioneService.create(
                StagioneDto.builder().nome("2027/2028").corrente(true).build());

        assertThat(precedente.isCorrente()).isFalse();
        assertThat(risultato.isCorrente()).isTrue();
    }

    @Test
    void laPrimaStagioneCreataDiventaCorrenteAncheSenzaChiederlo() {
        when(stagioneRepository.existsByNome("2026/2027")).thenReturn(false);
        when(stagioneRepository.save(any(Stagione.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stagioneRepository.findByCorrenteTrue()).thenReturn(Optional.empty());

        StagioneDto risultato = stagioneService.create(
                StagioneDto.builder().nome("2026/2027").corrente(false).build());

        assertThat(risultato.isCorrente()).isTrue();
    }

    @Test
    void nonPuoEliminareLaStagioneCorrente() {
        Stagione corrente = Stagione.builder().id(1L).nome("2026/2027").corrente(true).build();
        when(stagioneRepository.findById(1L)).thenReturn(Optional.of(corrente));

        assertThatThrownBy(() -> stagioneService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stagione corrente");
    }
}
