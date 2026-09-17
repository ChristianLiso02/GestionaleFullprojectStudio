package com.fullprojectstudio.frontend.security;

import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    public static final String TOKEN = "authToken";
    public static final String USERNAME = "authUsername";
    public static final String NOME = "authNome";
    public static final String COGNOME = "authCognome";
    public static final String RUOLO = "authRuolo";

    private SessionUtils() {
    }

    public static String getToken(HttpSession session) {
        return (String) session.getAttribute(TOKEN);
    }

    public static boolean isAuthenticated(HttpSession session) {
        return getToken(session) != null;
    }

    public static String getRuolo(HttpSession session) {
        return (String) session.getAttribute(RUOLO);
    }

    public static boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(getRuolo(session));
    }
}
