const STILI_BALLO = ["SALSA_CUBANA", "SALSA_LOS_ANGELES", "SALSA_PORTORICANA", "RUEDA_DE_CASINO", "BACHATA", "KIZOMBA", "CHACHACHA", "ALTRO"];

let istruttoriCache = [];

async function loadIstruttori() {
  try {
    istruttoriCache = await api.get("/api/istruttori");
    renderIstruttori();
  } catch (err) {
    showError(err.message);
  }
}

function renderIstruttori() {
  const body = document.getElementById("istruttoriBody");
  body.innerHTML = istruttoriCache.length === 0
    ? `<tr><td colspan="6" class="empty-state">Nessun istruttore registrato.</td></tr>`
    : istruttoriCache.map(i => `
        <tr>
          <td><b>${escapeHtml(i.nome)} ${escapeHtml(i.cognome)}</b></td>
          <td>${escapeHtml(i.telefono)}</td>
          <td>${escapeHtml(i.email)}</td>
          <td>${(i.specializzazioni || []).join(", ")}</td>
          <td><span class="pill ${i.attivo ? "pill-success" : "pill-muted"}">${i.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaIstruttore(${i.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaIstruttore(${i.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function istruttoreForm(i) {
  i = i || { nome: "", cognome: "", telefono: "", email: "", specializzazioni: [], compensoOrario: "", attivo: true };
  const spec = new Set(i.specializzazioni || []);
  return `
    <div class="row2">
      <div class="field"><label>Nome</label><input id="fNome" value="${escapeHtml(i.nome)}"></div>
      <div class="field"><label>Cognome</label><input id="fCognome" value="${escapeHtml(i.cognome)}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Telefono</label><input id="fTelefono" value="${escapeHtml(i.telefono)}"></div>
      <div class="field"><label>Email</label><input id="fEmail" type="email" value="${escapeHtml(i.email)}"></div>
    </div>
    <div class="field"><label>Compenso orario (€)</label><input id="fCompenso" type="number" step="0.01" value="${i.compensoOrario ?? ""}"></div>
    <div class="field">
      <label>Specializzazioni</label>
      <div class="checkbox-list">
        ${STILI_BALLO.map(s => `<label><input type="checkbox" value="${s}" class="fSpec" ${spec.has(s) ? "checked" : ""}> ${s}</label>`).join("")}
      </div>
    </div>
    <div class="field"><label><input type="checkbox" id="fAttivo" ${i.attivo ? "checked" : ""} style="width:auto;display:inline-block"> Attivo</label></div>
  `;
}

function readIstruttoreForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    cognome: document.getElementById("fCognome").value.trim(),
    telefono: document.getElementById("fTelefono").value.trim(),
    email: document.getElementById("fEmail").value.trim(),
    compensoOrario: document.getElementById("fCompenso").value ? parseFloat(document.getElementById("fCompenso").value) : null,
    specializzazioni: Array.from(document.querySelectorAll(".fSpec:checked")).map(el => el.value),
    attivo: document.getElementById("fAttivo").checked
  };
}

function nuovoIstruttore() {
  openModal("Nuovo istruttore", istruttoreForm(), async () => {
    const dto = readIstruttoreForm();
    if (!dto.nome || !dto.cognome) { showError("Nome e cognome sono obbligatori."); return false; }
    try {
      await api.post("/api/istruttori", dto);
      await loadIstruttori();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaIstruttore(id) {
  const i = istruttoriCache.find(x => x.id === id);
  openModal("Modifica istruttore", istruttoreForm(i), async () => {
    const dto = readIstruttoreForm();
    if (!dto.nome || !dto.cognome) { showError("Nome e cognome sono obbligatori."); return false; }
    try {
      await api.put(`/api/istruttori/${id}`, dto);
      await loadIstruttori();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaIstruttore(id) {
  if (!confirm("Eliminare questo istruttore?")) return;
  try {
    await api.del(`/api/istruttori/${id}`);
    await loadIstruttori();
  } catch (err) { showError(err.message); }
}

loadIstruttori();
