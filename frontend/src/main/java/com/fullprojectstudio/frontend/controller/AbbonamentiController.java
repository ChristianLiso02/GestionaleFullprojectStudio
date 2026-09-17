package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.TipoAbbonamentoApiClient;
import com.fullprojectstudio.frontend.dto.TipoAbbonamentoDto;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/abbonamenti")
@RequiredArgsConstructor
public class AbbonamentiController {

    private final TipoAbbonamentoApiClient tipoAbbonamentoApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("abbonamenti", tipoAbbonamentoApiClient.findAll(SessionUtils.getToken(session)));
        return "abbonamenti/list";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        TipoAbbonamentoDto dto = new TipoAbbonamentoDto();
        dto.setAttivo(true);
        model.addAttribute("abbonamento", dto);
        return "abbonamenti/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("abbonamento", tipoAbbonamentoApiClient.findById(SessionUtils.getToken(session), id));
        return "abbonamenti/form";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("abbonamento") TipoAbbonamentoDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            tipoAbbonamentoApiClient.create(token, dto);
        } else {
            tipoAbbonamentoApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/abbonamenti";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        tipoAbbonamentoApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/abbonamenti";
    }
}
