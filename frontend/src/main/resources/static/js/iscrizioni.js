const STATI_ISCRIZIONE = ["ATTIVA", "SCADUTA", "ANNULLATA"];

let iscrizioniCache = [];
let studentiLookup = [];
let corsiLookup = [];
let abbonamentiLookup = [];

async function loadIscrizioni() {
  try {
    [iscrizioniCache, studentiLookup, corsiLookup, abbonamentiLookup] = await Promise.all([
      api.get("/api/iscrizioni"),
      api.get("/api/studenti"),
      api.get("/api/corsi"),
      api.get("/api/abbonamenti")
    ]);
    renderIscrizioni();
  } catch (err) {
    showError(err.message);
  }
}

function renderIscrizioni() {
  const body = document.getElementById("iscrizioniBody");
  body.innerHTML = iscrizioniCache.length === 0
    ? `<tr><td colspan="7" class="empty-state">Nessuna iscrizione registrata.</td></tr>`
    : iscrizioniCache.map(i => `
        <tr>
          <td><b>${escapeHtml(i.studenteNomeCompleto)}</b></td>
          <td>${escapeHtml(i.corsoNome)}</td>
          <td>${escapeHtml(i.tipoAbbonamentoNome) || "-"}</td>
          <td>${formatDate(i.dataIscrizione)}</td>
          <td>${i.dataScadenza ? formatDate(i.dataScadenza) : "-"}</td>
          <td><span class="pill ${pillClass(i.stato)}">${i.stato}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaIscrizione(${i.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaIscrizione(${i.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function iscrizioneForm(i) {
  i = i || { studenteId: "", corsoId: "", tipoAbbonamentoId: "", dataIscrizione: todayISO(), dataScadenza: "", stato: "ATTIVA", note: "" };
  return `
    <div class="field"><label>Studente</label><select id="fStudente"><option value="">-- seleziona --</option>${studentiLookup.map(s => `<option value="${s.id}" ${i.studenteId === s.id ? "selected" : ""}>${escapeHtml(s.nome)} ${escapeHtml(s.cognome)}</option>`).join("")}</select></div>
    <div class="field"><label>Corso</label><select id="fCorso"><option value="">-- seleziona --</option>${corsiLookup.map(c => `<option value="${c.id}" ${i.corsoId === c.id ? "selected" : ""}>${escapeHtml(c.nome)}</option>`).join("")}</select></div>
    <div class="field"><label>Tipo abbonamento</label><select id="fAbbonamento"><option value="">-- nessuno --</option>${abbonamentiLookup.map(a => `<option value="${a.id}" ${i.tipoAbbonamentoId === a.id ? "selected" : ""}>${escapeHtml(a.nome)}</option>`).join("")}</select></div>
    <div class="row2">
      <div class="field"><label>Data iscrizione</label><input id="fDataIscr" type="date" value="${i.dataIscrizione || todayISO()}"></div>
      <div class="field"><label>Data scadenza</label><input id="fDataScad" type="date" value="${i.dataScadenza || ""}"></div>
    </div>
    <div class="field"><label>Stato</label><select id="fStato">${STATI_ISCRIZIONE.map(s => `<option value="${s}" ${i.stato === s ? "selected" : ""}>${s}</option>`).join("")}</select></div>
    <div class="field"><label>Note</label><textarea id="fNote" rows="2">${escapeHtml(i.note)}</textarea></div>
  `;
}

function readIscrizioneForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    corsoId: parseInt(document.getElementById("fCorso").value),
    tipoAbbonamentoId: document.getElementById("fAbbonamento").value ? parseInt(document.getElementById("fAbbonamento").value) : null,
    dataIscrizione: document.getElementById("fDataIscr").value || todayISO(),
    dataScadenza: document.getElementById("fDataScad").value || null,
    stato: document.getElementById("fStato").value,
    note: document.getElementById("fNote").value.trim()
  };
}

function nuovaIscrizione() {
  openModal("Nuova iscrizione", iscrizioneForm(), async () => {
    const dto = readIscrizioneForm();
    if (!dto.studenteId || !dto.corsoId) { showError("Studente e corso sono obbligatori."); return false; }
    try {
      await api.post("/api/iscrizioni", dto);
      await loadIscrizioni();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaIscrizione(id) {
  const i = iscrizioniCache.find(x => x.id === id);
  openModal("Modifica iscrizione", iscrizioneForm(i), async () => {
    const dto = readIscrizioneForm();
    if (!dto.studenteId || !dto.corsoId) { showError("Studente e corso sono obbligatori."); return false; }
    try {
      await api.put(`/api/iscrizioni/${id}`, dto);
      await loadIscrizioni();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaIscrizione(id) {
  if (!confirm("Eliminare questa iscrizione?")) return;
  try {
    await api.del(`/api/iscrizioni/${id}`);
    await loadIscrizioni();
  } catch (err) { showError(err.message); }
}

loadIscrizioni();
