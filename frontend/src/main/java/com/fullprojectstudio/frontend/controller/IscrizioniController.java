package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.CorsoApiClient;
import com.fullprojectstudio.frontend.client.IscrizioneApiClient;
import com.fullprojectstudio.frontend.client.StudenteApiClient;
import com.fullprojectstudio.frontend.client.TipoAbbonamentoApiClient;
import com.fullprojectstudio.frontend.dto.IscrizioneDto;
import com.fullprojectstudio.frontend.model.StatoIscrizione;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/iscrizioni")
@RequiredArgsConstructor
public class IscrizioniController {

    private final IscrizioneApiClient iscrizioneApiClient;
    private final StudenteApiClient studenteApiClient;
    private final CorsoApiClient corsoApiClient;
    private final TipoAbbonamentoApiClient tipoAbbonamentoApiClient;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("iscrizioni", iscrizioneApiClient.findAll(SessionUtils.getToken(session)));
        return "iscrizioni/list";
    }

    @GetMapping("/nuova")
    public String nuova(Model model, HttpSession session) {
        IscrizioneDto dto = new IscrizioneDto();
        dto.setStato(StatoIscrizione.ATTIVA);
        model.addAttribute("iscrizione", dto);
        addLookups(model, session);
        return "iscrizioni/form";
    }

    @GetMapping("/{id}/modifica")
    public String modifica(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("iscrizione", iscrizioneApiClient.findById(SessionUtils.getToken(session), id));
        addLookups(model, session);
        return "iscrizioni/form";
    }

    @PostMapping("/salva")
    public String salva(@ModelAttribute("iscrizione") IscrizioneDto dto, HttpSession session) {
        String token = SessionUtils.getToken(session);
        if (dto.getId() == null) {
            iscrizioneApiClient.create(token, dto);
        } else {
            iscrizioneApiClient.update(token, dto.getId(), dto);
        }
        return "redirect:/iscrizioni";
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id, HttpSession session) {
        iscrizioneApiClient.delete(SessionUtils.getToken(session), id);
        return "redirect:/iscrizioni";
    }

    private void addLookups(Model model, HttpSession session) {
        String token = SessionUtils.getToken(session);
        model.addAttribute("studenti", studenteApiClient.findAll(token, null));
        model.addAttribute("corsi", corsoApiClient.findAll(token));
        model.addAttribute("tipiAbbonamento", tipoAbbonamentoApiClient.findAll(token));
        model.addAttribute("statiIscrizione", StatoIscrizione.values());
    }
}
