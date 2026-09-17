package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.SalaApiClient;
import com.fullprojectstudio.frontend.dto.SalaDto;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SalaApiClient salaApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("sale", salaApiClient.findAll(SessionUtils.getToken(session)));
        return "sale/list";
    }

    @GetMapping("/nuova")
    public String nuova(Model model) {
        model.addAttribute("sala", new SalaDto());
        return "sale/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("sala", salaApiClient.findById(SessionUtils.getToken(session), id));
        return "sale/form";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("sala") SalaDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            salaApiClient.create(token, dto);
        } else {
            salaApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/sale";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        salaApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/sale";
    }
}
