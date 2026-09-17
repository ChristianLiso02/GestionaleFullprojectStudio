package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.CorsoApiClient;
import com.fullprojectstudio.frontend.client.IstruttoreApiClient;
import com.fullprojectstudio.frontend.client.SalaApiClient;
import com.fullprojectstudio.frontend.dto.CorsoDto;
import com.fullprojectstudio.frontend.model.Livello;
import com.fullprojectstudio.frontend.model.StileBallo;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/corsi")
@RequiredArgsConstructor
public class CorsiController {

    private final CorsoApiClient corsoApiClient;
    private final IstruttoreApiClient istruttoreApiClient;
    private final SalaApiClient salaApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("corsi", corsoApiClient.findAll(SessionUtils.getToken(session)));
        return "corsi/list";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model, HttpSession session) {
        CorsoDto dto = new CorsoDto();
        dto.setAttivo(true);
        model.addAttribute("corso", dto);
        addLookups(model, session);
        return "corsi/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("corso", corsoApiClient.findById(SessionUtils.getToken(session), id));
        addLookups(model, session);
        return "corsi/form";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("corso", corsoApiClient.findById(SessionUtils.getToken(session), id));
        return "corsi/dettaglio";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("corso") CorsoDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            corsoApiClient.create(token, dto);
        } else {
            corsoApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/corsi";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        corsoApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/corsi";
    }

    private void addLookups(Model model, HttpSession session) {
        String token = SessionUtils.getToken(session);
        model.addAttribute("istruttori", istruttoreApiClient.findAll(token));
        model.addAttribute("sale", salaApiClient.findAll(token));
        model.addAttribute("stiliBallo", StileBallo.values());
        model.addAttribute("livelli", Livello.values());
        model.addAttribute("giorni", DayOfWeek.values());
    }
}
