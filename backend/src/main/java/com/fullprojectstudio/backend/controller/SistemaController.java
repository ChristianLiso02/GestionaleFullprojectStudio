package com.fullprojectstudio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Risposta pubblica e minima per riconoscere il gestionale: il programma installato sul PC la usa per capire
 * se sull'indirizzo risponde davvero lui (e non un altro programma, come una versione di prova con Docker).
 */
@RestController
@RequestMapping("/api/sistema")
@RequiredArgsConstructor
public class SistemaController {

    static final String NOME = "FullProjectStudio";

    private final Environment environment;

    @GetMapping
    public Map<String, String> info() {
        return Map.of(
                "applicazione", NOME,
                "modalita", environment.acceptsProfiles(Profiles.of("desktop")) ? "desktop" : "server");
    }
}
