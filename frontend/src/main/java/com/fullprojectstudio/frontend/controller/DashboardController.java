package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.DashboardApiClient;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardApiClient dashboardApiClient;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("stats", dashboardApiClient.getStats(SessionUtils.getToken(session)));
        return "dashboard";
    }
}
