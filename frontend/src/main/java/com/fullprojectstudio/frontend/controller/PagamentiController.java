package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.IscrizioneApiClient;
import com.fullprojectstudio.frontend.client.PagamentoApiClient;
import com.fullprojectstudio.frontend.client.StudenteApiClient;
import com.fullprojectstudio.frontend.dto.PagamentoDto;
import com.fullprojectstudio.frontend.model.MetodoPagamento;
import com.fullprojectstudio.frontend.model.StatoPagamento;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/pagamenti")
@RequiredArgsConstructor
public class PagamentiController {

    private final PagamentoApiClient pagamentoApiClient;
    private final StudenteApiClient studenteApiClient;
    private final IscrizioneApiClient iscrizioneApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("pagamenti", pagamentoApiClient.findAll(SessionUtils.getToken(session)));
        return "pagamenti/list";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model, HttpSession session) {
        PagamentoDto dto = new PagamentoDto();
        dto.setDataPagamento(LocalDate.now());
        dto.setStato(StatoPagamento.PAGATO);
        model.addAttribute("pagamento", dto);
        addLookups(model, session);
        return "pagamenti/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("pagamento", pagamentoApiClient.findById(SessionUtils.getToken(session), id));
        addLookups(model, session);
        return "pagamenti/form";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("pagamento") PagamentoDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            pagamentoApiClient.create(token, dto);
        } else {
            pagamentoApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/pagamenti";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        pagamentoApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/pagamenti";
    }

    private void addLookups(Model model, HttpSession session) {
        String token = SessionUtils.getToken(session);
        model.addAttribute("studenti", studenteApiClient.findAll(token, null));
        model.addAttribute("iscrizioni", iscrizioneApiClient.findAll(token));
        model.addAttribute("metodiPagamento", MetodoPagamento.values());
        model.addAttribute("statiPagamento", StatoPagamento.values());
    }
}
