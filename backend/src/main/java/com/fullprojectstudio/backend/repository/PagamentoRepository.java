package com.fullprojectstudio.backend.repository;

import com.fullprojectstudio.backend.model.Pagamento;
import com.fullprojectstudio.backend.model.StatoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByStudenteId(Long studenteId);
    List<Pagamento> findByStato(StatoPagamento stato);
    List<Pagamento> findByDataPagamentoBetween(LocalDate da, LocalDate a);
    List<Pagamento> findByIscrizioneIdInAndStato(Collection<Long> iscrizioniIds, StatoPagamento stato);
    List<Pagamento> findByIscrizioneIsNullAndStatoAndDataPagamentoBetween(StatoPagamento stato, LocalDate da, LocalDate a);
}
