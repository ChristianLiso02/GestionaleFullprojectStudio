package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.StudenteDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudenteService {

    private final StudenteRepository studenteRepository;

    public List<StudenteDto> findAll() {
        return studenteRepository.findAll().stream().map(this::toDto).toList();
    }

    public StudenteDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public List<StudenteDto> ricerca(String testo) {
        return studenteRepository.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(testo, testo)
                .stream().map(this::toDto).toList();
    }

    public StudenteDto create(StudenteDto dto) {
        String codiceFiscale = normalizzaCodiceFiscale(dto.getCodiceFiscale());
        if (codiceFiscale == null) {
            throw new IllegalArgumentException("Il codice fiscale è obbligatorio.");
        }
        if (studenteRepository.existsByCodiceFiscaleIgnoreCase(codiceFiscale)) {
            throw new IllegalArgumentException("Esiste già uno studente con il codice fiscale " + codiceFiscale + ".");
        }
        Studente studente = fromDto(dto);
        studente.setId(null);
        studente.setCodiceFiscale(codiceFiscale);
        return toDto(studenteRepository.save(studente));
    }

    public StudenteDto update(Long id, StudenteDto dto) {
        Studente studente = getEntity(id);
        studente.setNome(dto.getNome());
        studente.setCognome(dto.getCognome());
        studente.setSesso(dto.getSesso());
        // Obbligatorio solo in creazione: gli studenti inseriti prima si possono modificare anche senza,
        // ma un codice fiscale già presente non si può togliere.
        String codiceFiscale = normalizzaCodiceFiscale(dto.getCodiceFiscale());
        if (codiceFiscale == null && studente.getCodiceFiscale() != null) {
            throw new IllegalArgumentException("Il codice fiscale non può essere tolto.");
        }
        if (codiceFiscale != null && studenteRepository.existsByCodiceFiscaleIgnoreCaseAndIdNot(codiceFiscale, id)) {
            throw new IllegalArgumentException("Esiste già uno studente con il codice fiscale " + codiceFiscale + ".");
        }
        studente.setCodiceFiscale(codiceFiscale);
        studente.setDataNascita(dto.getDataNascita());
        studente.setTelefono(dto.getTelefono());
        studente.setEmail(dto.getEmail());
        studente.setIndirizzo(dto.getIndirizzo());
        studente.setContattoEmergenza(dto.getContattoEmergenza());
        studente.setNoteMediche(dto.getNoteMediche());
        studente.setAttivo(dto.isAttivo());
        return toDto(studenteRepository.save(studente));
    }

    public void delete(Long id) {
        Studente studente = getEntity(id);
        studenteRepository.delete(studente);
    }

    private String normalizzaCodiceFiscale(String value) {
        return (value == null || value.isBlank()) ? null : value.trim().toUpperCase();
    }

    private Studente getEntity(Long id) {
        return studenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + id));
    }

    private StudenteDto toDto(Studente s) {
        return StudenteDto.builder()
                .id(s.getId())
                .nome(s.getNome())
                .cognome(s.getCognome())
                .sesso(s.getSesso())
                .codiceFiscale(s.getCodiceFiscale())
                .dataNascita(s.getDataNascita())
                .telefono(s.getTelefono())
                .email(s.getEmail())
                .indirizzo(s.getIndirizzo())
                .contattoEmergenza(s.getContattoEmergenza())
                .noteMediche(s.getNoteMediche())
                .dataIscrizione(s.getDataIscrizione())
                .attivo(s.isAttivo())
                .build();
    }

    private Studente fromDto(StudenteDto dto) {
        return Studente.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .sesso(dto.getSesso())
                .codiceFiscale(normalizzaCodiceFiscale(dto.getCodiceFiscale()))
                .dataNascita(dto.getDataNascita())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .indirizzo(dto.getIndirizzo())
                .contattoEmergenza(dto.getContattoEmergenza())
                .noteMediche(dto.getNoteMediche())
                .attivo(dto.isAttivo())
                .build();
    }
}
