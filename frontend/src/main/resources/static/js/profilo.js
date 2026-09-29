function mostraTesta(p) {
  const ruolo = p.ruolo === "ADMIN" ? "Amministratore" : p.ruolo === "SEGRETERIA" ? "Segreteria" : p.ruolo;
  document.getElementById("profiloAvatar").textContent = ((p.nome || "?")[0] + (p.cognome || "?")[0]).toUpperCase();
  document.getElementById("profiloNome").textContent = `${p.nome || ""} ${p.cognome || ""}`;
  document.getElementById("profiloAccount").textContent = `${p.username} · ${ruolo}`;
}

async function caricaProfilo() {
  try {
    const p = await api.get("/api/auth/profilo");
    mostraTesta(p);
    document.getElementById("fNome").value = p.nome || "";
    document.getElementById("fCognome").value = p.cognome || "";
    document.getElementById("fEmail").value = p.email || "";
    document.getElementById("fUsername").value = p.username;
  } catch (err) {
    showError(err.message);
  }
}

document.getElementById("profiloForm").addEventListener("submit", async e => {
  e.preventDefault();
  const form = e.target;
  clearAllFieldErrors(form);
  document.getElementById("successBox").hidden = true;
  const dto = {
    nome: document.getElementById("fNome").value.trim(),
    cognome: document.getElementById("fCognome").value.trim(),
    email: document.getElementById("fEmail").value.trim()
  };
  if (!dto.nome) { setFieldError("fNome", "Il nome è obbligatorio."); return; }
  if (!dto.cognome) { setFieldError("fCognome", "Il cognome è obbligatorio."); return; }
  if (dto.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dto.email)) { setFieldError("fEmail", "Email non valida."); return; }
  try {
    const p = await api.put("/api/auth/profilo", dto);
    // Aggiorna nome e iniziali mostrati in alto senza dover rifare il login
    const utente = getUser() || {};
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify({ ...utente, nome: p.nome, cognome: p.cognome }));
    renderNav("profilo", "Profilo", "I dati del tuo account");
    mostraTesta(p);
    const box = document.getElementById("successBox");
    box.textContent = "Dati salvati.";
    box.hidden = false;
  } catch (err) {
    showError(err.message);
  }
});

caricaProfilo();
