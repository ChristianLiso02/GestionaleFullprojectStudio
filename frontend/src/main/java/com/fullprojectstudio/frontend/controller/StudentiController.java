package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.IscrizioneApiClient;
import com.fullprojectstudio.frontend.client.PagamentoApiClient;
import com.fullprojectstudio.frontend.client.StudenteApiClient;
import com.fullprojectstudio.frontend.dto.StudenteDto;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/studenti")
@RequiredArgsConstructor
public class StudentiController {

    private final StudenteApiClient studenteApiClient;
    private final IscrizioneApiClient iscrizioneApiClient;
    private final PagamentoApiClient pagamentoApiClient;

    @GetMapping
    public String list(@RequestParam(required = false) String ricerca, Model model, HttpSession session) {
        model.addAttribute("studenti", studenteApiClient.findAll(SessionUtils.getToken(session), ricerca));
        model.addAttribute("ricerca", ricerca);
        return "studenti/list";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        StudenteDto dto = new StudenteDto();
        dto.setAttivo(true);
        model.addAttribute("studente", dto);
        return "studenti/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("studente", studenteApiClient.findById(SessionUtils.getToken(session), id));
        return "studenti/form";
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model, HttpSession session) {
        String token = SessionUtils.getToken(session);
        model.addAttribute("studente", studenteApiClient.findById(token, id));
        model.addAttribute("iscrizioni", iscrizioneApiClient.findByStudente(token, id));
        model.addAttribute("pagamenti", pagamentoApiClient.findByStudente(token, id));
        return "studenti/dettaglio";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("studente") StudenteDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            studenteApiClient.create(token, dto);
        } else {
            studenteApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/studenti";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        studenteApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/studenti";
    }
}
