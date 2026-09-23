package com.fullprojectstudio.backend.service;

import com.fullprojectstudio.backend.dto.PagamentoDto;
import com.fullprojectstudio.backend.exception.ResourceNotFoundException;
import com.fullprojectstudio.backend.model.Iscrizione;
import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.StatoPagamento;
import com.fullprojectstudio.backend.model.Studente;
import com.fullprojectstudio.backend.repository.IscrizioneRepository;
import com.fullprojectstudio.backend.repository.PagamentoRepository;
import com.fullprojectstudio.backend.repository.StudenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final StudenteRepository studenteRepository;
    private final IscrizioneRepository iscrizioneRepository;

    public List<PagamentoDto> findAll() {
        return pagamentoRepository.findAll().stream().map(this::toDto).toList();
    }

    public PagamentoDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public List<PagamentoDto> findByStudente(Long studenteId) {
        return pagamentoRepository.findByStudenteId(studenteId).stream().map(this::toDto).toList();
    }

    public List<PagamentoDto> findByStato(StatoPagamento stato) {
        return pagamentoRepository.findByStato(stato).stream().map(this::toDto).toList();
    }

    public PagamentoDto create(PagamentoDto dto) {
        Pagamento pagamento = new Pagamento();
        applyDto(pagamento, dto);
        if (pagamento.getDataPagamento() == null) {
            pagamento.setDataPagamento(LocalDate.now());
        }
        return toDto(pagamentoRepository.save(pagamento));
    }

    public PagamentoDto update(Long id, PagamentoDto dto) {
        Pagamento pagamento = getEntity(id);
        applyDto(pagamento, dto);
        return toDto(pagamentoRepository.save(pagamento));
    }

    public void delete(Long id) {
        pagamentoRepository.delete(getEntity(id));
    }

    private void applyDto(Pagamento pagamento, PagamentoDto dto) {
        Studente studente = studenteRepository.findById(dto.getStudenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + dto.getStudenteId()));
        pagamento.setStudente(studente);

        if (dto.getIscrizioneId() != null) {
            Iscrizione iscrizione = iscrizioneRepository.findById(dto.getIscrizioneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata: " + dto.getIscrizioneId()));
            pagamento.setIscrizione(iscrizione);
        } else {
            pagamento.setIscrizione(null);
        }

        pagamento.setImporto(dto.getImporto());
        if (dto.getDataPagamento() != null) {
            pagamento.setDataPagamento(dto.getDataPagamento());
        }
        pagamento.setMetodo(dto.getMetodo());
        pagamento.setCausale(dto.getCausale());
        pagamento.setMeseRiferimento(dto.getMeseRiferimento() != null ? dto.getMeseRiferimento().atDay(1) : null);
        pagamento.setMesiCoperti(dto.getMesiCoperti() != null ? dto.getMesiCoperti() : 1);
        pagamento.setStato(dto.getStato() != null ? dto.getStato() : StatoPagamento.PAGATO);
        pagamento.setNote(dto.getNote());
    }

    private Pagamento getEntity(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento non trovato: " + id));
    }

    private PagamentoDto toDto(Pagamento p) {
        return PagamentoDto.builder()
                .id(p.getId())
                .studenteId(p.getStudente().getId())
                .studenteNomeCompleto(p.getStudente().getNome() + " " + p.getStudente().getCognome())
                .iscrizioneId(p.getIscrizione() != null ? p.getIscrizione().getId() : null)
                .importo(p.getImporto())
                .dataPagamento(p.getDataPagamento())
                .metodo(p.getMetodo())
                .causale(p.getCausale())
                .meseRiferimento(p.getMeseRiferimento() != null ? YearMonth.from(p.getMeseRiferimento()) : null)
                .mesiCoperti(p.getMesiCoperti() != null ? p.getMesiCoperti() : 1)
                .stato(p.getStato())
                .note(p.getNote())
                .build();
    }
}
