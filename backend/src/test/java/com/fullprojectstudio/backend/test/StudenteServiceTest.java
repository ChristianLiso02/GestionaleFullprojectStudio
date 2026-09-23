package com.fullprojectstudio.backend.test;

import com.fullprojectstudio.backend.dto.StudenteDto;
import com.fullprojectstudio.backend.model.Sesso;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import com.fullprojectstudio.backend.service.StudenteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Codice fiscale obbligatorio in creazione, unico e mai cancellabile; gli studenti inseriti
 * prima dell'obbligo restano modificabili anche senza.
 */
@ExtendWith(MockitoExtension.class)
class StudenteServiceTest {

    private static final String CF = "RSSMRA80A01H501U";

    @Mock private StudenteRepository studenteRepository;

    @InjectMocks private StudenteService studenteService;

    private StudenteDto dto(String codiceFiscale) {
        return StudenteDto.builder().nome("Mario").cognome("Rossi").sesso(Sesso.UOMO)
                .codiceFiscale(codiceFiscale).attivo(true).build();
    }

    @Test
    void ilCodiceFiscaleEObbligatorioInCreazione() {
        assertThatThrownBy(() -> studenteService.create(dto("")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obbligatorio");
    }

    @Test
    void ilCodiceFiscaleVieneSalvatoInMaiuscolo() {
        when(studenteRepository.save(any(Studente.class))).thenAnswer(inv -> inv.getArgument(0));

        studenteService.create(dto("rssmra80a01h501u"));

        ArgumentCaptor<Studente> captor = ArgumentCaptor.forClass(Studente.class);
        verify(studenteRepository).save(captor.capture());
        assertThat(captor.getValue().getCodiceFiscale()).isEqualTo(CF);
    }

    @Test
    void nonSiCreaUnDoppioneConLoStessoCodiceFiscale() {
        when(studenteRepository.existsByCodiceFiscaleIgnoreCase(CF)).thenReturn(true);

        assertThatThrownBy(() -> studenteService.create(dto(CF)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Esiste già");
    }

    @Test
    void unoStudenteInseritoPrimaSenzaCodiceFiscaleSiPuoAncoraModificare() {
        when(studenteRepository.findById(5L)).thenReturn(Optional.of(Studente.builder().id(5L).nome("Mario").cognome("Rossi").build()));
        when(studenteRepository.save(any(Studente.class))).thenAnswer(inv -> inv.getArgument(0));

        StudenteDto risultato = studenteService.update(5L, dto(""));

        assertThat(risultato.getCodiceFiscale()).isNull();
    }

    @Test
    void unCodiceFiscaleGiaPresenteNonSiPuoTogliere() {
        when(studenteRepository.findById(5L)).thenReturn(Optional.of(Studente.builder().id(5L).codiceFiscale(CF).build()));

        assertThatThrownBy(() -> studenteService.update(5L, dto("")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non può essere tolto");
    }

    @Test
    void salvaIlSessoDelloStudente() {
        when(studenteRepository.save(any(Studente.class))).thenAnswer(inv -> inv.getArgument(0));

        StudenteDto risultato = studenteService.create(StudenteDto.builder()
                .nome("Anna").cognome("Verdi").sesso(Sesso.DONNA).codiceFiscale("VRDNNA90A41H501X").attivo(true).build());

        assertThat(risultato.getSesso()).isEqualTo(Sesso.DONNA);
    }
}
