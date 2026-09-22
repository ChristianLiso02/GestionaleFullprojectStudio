package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.StudenteDto;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import com.fullprojectstudio.backend.service.StudenteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica che un codice fiscale vuoto venga salvato come NULL (non come
 * stringa vuota), altrimenti il vincolo di unicità a livello di database
 * blocca il secondo studente senza codice fiscale.
 */
@ExtendWith(MockitoExtension.class)
class StudenteServiceTest {

    @Mock private StudenteRepository studenteRepository;

    @InjectMocks private StudenteService studenteService;

    @Test
    void codiceFiscaleVuotoVieneSalvatoComeNull() {
        when(studenteRepository.save(any(Studente.class))).thenAnswer(inv -> inv.getArgument(0));

        studenteService.create(StudenteDto.builder()
                .nome("Mario").cognome("Rossi").codiceFiscale("").attivo(true).build());

        ArgumentCaptor<Studente> captor = ArgumentCaptor.forClass(Studente.class);
        verify(studenteRepository).save(captor.capture());
        assertThat(captor.getValue().getCodiceFiscale()).isNull();
    }

    @Test
    void codiceFiscaleValorizzatoVieneMantenuto() {
        when(studenteRepository.save(any(Studente.class))).thenAnswer(inv -> inv.getArgument(0));

        studenteService.create(StudenteDto.builder()
                .nome("Mario").cognome("Rossi").codiceFiscale("RSSMRA80A01H501U").attivo(true).build());

        ArgumentCaptor<Studente> captor = ArgumentCaptor.forClass(Studente.class);
        verify(studenteRepository).save(captor.capture());
        assertThat(captor.getValue().getCodiceFiscale()).isEqualTo("RSSMRA80A01H501U");
    }
}
