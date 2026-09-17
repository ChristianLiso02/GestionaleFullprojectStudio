let studentiCache = [];

async function loadStudenti(ricerca) {
  try {
    const query = ricerca ? `?ricerca=${encodeURIComponent(ricerca)}` : "";
    studentiCache = await api.get("/api/studenti" + query);
    renderStudenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderStudenti() {
  const body = document.getElementById("studentiBody");
  body.innerHTML = studentiCache.length === 0
    ? `<tr><td colspan="5" class="empty-state">Nessuno studente trovato.</td></tr>`
    : studentiCache.map(s => `
        <tr>
          <td><b>${escapeHtml(s.nome)} ${escapeHtml(s.cognome)}</b></td>
          <td>${escapeHtml(s.telefono)}<br><span style="color:var(--ink-faint);font-size:0.78rem">${escapeHtml(s.email)}</span></td>
          <td>${formatDate(s.dataIscrizione)}</td>
          <td><span class="pill ${s.attivo ? "pill-success" : "pill-muted"}">${s.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaStudente(${s.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaStudente(${s.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function studenteForm(s) {
  s = s || { nome: "", cognome: "", codiceFiscale: "", dataNascita: "", telefono: "", email: "", indirizzo: "", contattoEmergenza: "", noteMediche: "", dataIscrizione: todayISO(), attivo: true };
  return `
    <div class="row2">
      <div class="field"><label>Nome</label><input id="fNome" value="${escapeHtml(s.nome)}"></div>
      <div class="field"><label>Cognome</label><input id="fCognome" value="${escapeHtml(s.cognome)}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Codice fiscale</label><input id="fCf" value="${escapeHtml(s.codiceFiscale)}"></div>
      <div class="field"><label>Data di nascita</label><input id="fNascita" type="date" value="${s.dataNascita || ""}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Telefono</label><input id="fTelefono" value="${escapeHtml(s.telefono)}"></div>
      <div class="field"><label>Email</label><input id="fEmail" type="email" value="${escapeHtml(s.email)}"></div>
    </div>
    <div class="field"><label>Indirizzo</label><input id="fIndirizzo" value="${escapeHtml(s.indirizzo)}"></div>
    <div class="field"><label>Contatto emergenza</label><input id="fEmergenza" value="${escapeHtml(s.contattoEmergenza)}"></div>
    <div class="field"><label>Data iscrizione</label><input id="fDataIscr" type="date" value="${s.dataIscrizione || todayISO()}"></div>
    <div class="field"><label>Note mediche / allergie</label><textarea id="fNote" rows="2">${escapeHtml(s.noteMediche)}</textarea></div>
    <div class="field"><label><input type="checkbox" id="fAttivo" ${s.attivo ? "checked" : ""} style="width:auto;display:inline-block"> Attivo</label></div>
  `;
}

function readStudenteForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    cognome: document.getElementById("fCognome").value.trim(),
    codiceFiscale: document.getElementById("fCf").value.trim(),
    dataNascita: document.getElementById("fNascita").value || null,
    telefono: document.getElementById("fTelefono").value.trim(),
    email: document.getElementById("fEmail").value.trim(),
    indirizzo: document.getElementById("fIndirizzo").value.trim(),
    contattoEmergenza: document.getElementById("fEmergenza").value.trim(),
    dataIscrizione: document.getElementById("fDataIscr").value || todayISO(),
    noteMediche: document.getElementById("fNote").value.trim(),
    attivo: document.getElementById("fAttivo").checked
  };
}

function nuovoStudente() {
  openModal("Nuovo studente", studenteForm(), async () => {
    const dto = readStudenteForm();
    if (!dto.nome || !dto.cognome) { showError("Nome e cognome sono obbligatori."); return false; }
    try {
      await api.post("/api/studenti", dto);
      await loadStudenti(document.getElementById("ricercaInput").value.trim());
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaStudente(id) {
  const s = studentiCache.find(x => x.id === id);
  openModal("Modifica studente", studenteForm(s), async () => {
    const dto = readStudenteForm();
    if (!dto.nome || !dto.cognome) { showError("Nome e cognome sono obbligatori."); return false; }
    try {
      await api.put(`/api/studenti/${id}`, dto);
      await loadStudenti(document.getElementById("ricercaInput").value.trim());
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaStudente(id) {
  if (!confirm("Eliminare questo studente? Se ha iscrizioni o pagamenti collegati, l'eliminazione verrà bloccata dal database.")) return;
  try {
    await api.del(`/api/studenti/${id}`);
    await loadStudenti(document.getElementById("ricercaInput").value.trim());
  } catch (err) { showError(err.message); }
}

let ricercaTimeout;
document.getElementById("ricercaInput").addEventListener("input", (e) => {
  clearTimeout(ricercaTimeout);
  ricercaTimeout = setTimeout(() => loadStudenti(e.target.value.trim()), 300);
});

loadStudenti();
