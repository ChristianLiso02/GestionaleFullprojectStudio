package com.fullprojectstudio.backend.controller;

import com.fullprojectstudio.backend.dto.StatisticheDto;
import com.fullprojectstudio.backend.service.StatisticheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistiche")
@RequiredArgsConstructor
public class StatisticheController {

    private final StatisticheService statisticheService;

    @GetMapping
    public StatisticheDto statistiche(@RequestParam(required = false) Long stagioneId) {
        return statisticheService.statistiche(stagioneId, LocalDate.now());
    }
}
