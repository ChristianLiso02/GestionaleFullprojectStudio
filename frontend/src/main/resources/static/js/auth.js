const AUTH_TOKEN_KEY = "fps_token";
const AUTH_USER_KEY = "fps_user";

function saveSession(loginResponse) {
  localStorage.setItem(AUTH_TOKEN_KEY, loginResponse.token);
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify({
    username: loginResponse.username,
    nome: loginResponse.nome,
    cognome: loginResponse.cognome,
    ruolo: loginResponse.ruolo
  }));
}

function getToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

function getUser() {
  const raw = localStorage.getItem(AUTH_USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

function isAuthenticated() {
  return !!getToken();
}

function clearSession() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_KEY);
}

// Da chiamare in cima ad ogni pagina protetta: se non sei loggato, torna al login.
function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = "login.html";
  }
}

function logout() {
  clearSession();
  window.location.href = "login.html";
}
