package com.fullprojectstudio.frontend.config;

import com.fullprojectstudio.frontend.security.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${app.branding.nome-scuola}")
    private String nomeScuola;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session) {
        model.addAttribute("nomeScuola", nomeScuola);
        model.addAttribute("nomeUtente", session.getAttribute(SessionUtils.NOME));
        model.addAttribute("cognomeUtente", session.getAttribute(SessionUtils.COGNOME));
        model.addAttribute("ruoloUtente", session.getAttribute(SessionUtils.RUOLO));
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public String handleUnauthorized(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login?scaduto";
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound() {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("messaggioErrore", "Risorsa non trovata.");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("messaggioErrore", "Si è verificato un errore: " + ex.getMessage());
        return mv;
    }
}
