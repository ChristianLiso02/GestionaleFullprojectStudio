package com.fullprojectstudio.frontend.controller;

import com.fullprojectstudio.frontend.client.AuthApiClient;
import com.fullprojectstudio.frontend.dto.LoginResponse;
import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthApiClient authApiClient;

    @GetMapping("/login")
    public String loginForm(HttpSession session, @RequestParam(required = false) String scaduto) {
        if (SessionUtils.isAuthenticated(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpSession session, Model model) {
        try {
            LoginResponse response = authApiClient.login(username, password);
            session.setAttribute(SessionUtils.TOKEN, response.getToken());
            session.setAttribute(SessionUtils.USERNAME, response.getUsername());
            session.setAttribute(SessionUtils.NOME, response.getNome());
            session.setAttribute(SessionUtils.COGNOME, response.getCognome());
            session.setAttribute(SessionUtils.RUOLO, response.getRuolo());
            return "redirect:/dashboard";
        } catch (HttpClientErrorException.Unauthorized ex) {
            model.addAttribute("errore", "Username o password non validi.");
            return "login";
        } catch (Exception ex) {
            model.addAttribute("errore", "Impossibile contattare il server. Riprova più tardi.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return SessionUtils.isAuthenticated(session) ? "redirect:/dashboard" : "redirect:/login";
    }
}
