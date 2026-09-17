package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.CorsoApiClient;
import com.fullprojectstudio.frontend.client.IscrizioneApiClient;
import com.fullprojectstudio.frontend.client.PresenzaApiClient;
import com.fullprojectstudio.frontend.dto.IscrizioneDto;
import com.fullprojectstudio.frontend.dto.PresenzaDto;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/presenze")
@RequiredArgsConstructor
public class PresenzeController {

    private final CorsoApiClient corsoApiClient;
    private final IscrizioneApiClient iscrizioneApiClient;
    private final PresenzaApiClient presenzaApiClient;

    @GetMapping
    public String appello(@RequestParam(required = false) Long corsoId,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                           Model model, HttpSession session) {
        String token = SessionUtils.getToken(session);
        model.addAttribute("corsi", corsoApiClient.findAll(token));
        model.addAttribute("corsoId", corsoId);
        LocalDate dataLezione = data != null ? data : LocalDate.now();
        model.addAttribute("data", dataLezione);

        if (corsoId != null) {
            List<IscrizioneDto> iscritti = iscrizioneApiClient.findAll(token).stream()
                    .filter(i -> i.getCorsoId().equals(corsoId))
                    .toList();
            Map<Long, PresenzaDto> presenzePerIscrizione = presenzaApiClient.findByCorsoEData(token, corsoId, dataLezione)
                    .stream().collect(Collectors.toMap(PresenzaDto::getIscrizioneId, p -> p));

            model.addAttribute("iscritti", iscritti);
            model.addAttribute("presenzePerIscrizione", presenzePerIscrizione);
        }

        return "presenze/appello";
    }

    @PostMapping("/registra")
    public String registra(@RequestParam Long iscrizioneId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                            @RequestParam Long corsoId,
                            @RequestParam(defaultValue = "false") boolean presente,
                            HttpSession session) {
        PresenzaDto dto = new PresenzaDto();
        dto.setIscrizioneId(iscrizioneId);
        dto.setDataLezione(data);
        dto.setPresente(presente);
        presenzaApiClient.registra(SessionUtils.getToken(session), dto);
        return "redirect:/presenze?corsoId=" + corsoId + "&data=" + data;
    }
}
