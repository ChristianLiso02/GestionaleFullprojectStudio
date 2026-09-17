package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.IstruttoreApiClient;
import com.fullprojectstudio.frontend.dto.IstruttoreDto;
import com.fullprojectstudio.frontend.model.StileBallo;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/istruttori")
@RequiredArgsConstructor
public class IstruttoriController {

    private final IstruttoreApiClient istruttoreApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("istruttori", istruttoreApiClient.findAll(SessionUtils.getToken(session)));
        return "istruttori/list";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        IstruttoreDto dto = new IstruttoreDto();
        dto.setAttivo(true);
        model.addAttribute("istruttore", dto);
        model.addAttribute("stiliBallo", StileBallo.values());
        return "istruttori/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("istruttore", istruttoreApiClient.findById(SessionUtils.getToken(session), id));
        model.addAttribute("stiliBallo", StileBallo.values());
        return "istruttori/form";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("istruttore") IstruttoreDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            istruttoreApiClient.create(token, dto);
        } else {
            istruttoreApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/istruttori";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        istruttoreApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/istruttori";
    }
}
