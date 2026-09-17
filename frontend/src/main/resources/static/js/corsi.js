const STILI_BALLO = ["SALSA_CUBANA", "SALSA_LOS_ANGELES", "SALSA_PORTORICANA", "RUEDA_DE_CASINO", "BACHATA", "KIZOMBA", "CHACHACHA", "ALTRO"];
const LIVELLI = ["BASE", "INTERMEDIO", "AVANZATO", "TUTTI_I_LIVELLI"];
const GIORNI = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const GIORNI_LABEL = { MONDAY: "Lun", TUESDAY: "Mar", WEDNESDAY: "Mer", THURSDAY: "Gio", FRIDAY: "Ven", SATURDAY: "Sab", SUNDAY: "Dom" };

let corsiCache = [];
let istruttoriLookup = [];
let saleLookup = [];

async function loadCorsi() {
  try {
    [corsiCache, istruttoriLookup, saleLookup] = await Promise.all([
      api.get("/api/corsi"),
      api.get("/api/istruttori"),
      api.get("/api/sale")
    ]);
    renderCorsi();
  } catch (err) {
    showError(err.message);
  }
}

function renderCorsi() {
  const body = document.getElementById("corsiBody");
  body.innerHTML = corsiCache.length === 0
    ? `<tr><td colspan="9" class="empty-state">Nessun corso registrato.</td></tr>`
    : corsiCache.map(c => `
        <tr>
          <td><b>${escapeHtml(c.nome)}</b></td>
          <td>${c.stile}</td>
          <td>${c.livello}</td>
          <td>${escapeHtml(c.istruttoreNome) || "-"}</td>
          <td>${escapeHtml(c.salaNome) || "-"}</td>
          <td>${(c.giorniSettimana || []).map(g => GIORNI_LABEL[g]).join(" · ")}<br>${c.orarioInizio || ""} - ${c.orarioFine || ""}</td>
          <td>${c.prezzoMensile != null ? formatEuro(c.prezzoMensile) : "-"}</td>
          <td><span class="pill ${c.attivo ? "pill-success" : "pill-muted"}">${c.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaCorso(${c.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaCorso(${c.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function corsoForm(c) {
  c = c || { nome: "", stile: "SALSA_CUBANA", livello: "BASE", istruttoreId: "", salaId: "", giorniSettimana: [], orarioInizio: "", orarioFine: "", capienzaMax: "", prezzoMensile: "", dataInizio: "", dataFine: "", attivo: true };
  const giorni = new Set(c.giorniSettimana || []);
  return `
    <div class="row2">
      <div class="field"><label>Nome corso</label><input id="fNome" value="${escapeHtml(c.nome)}"></div>
      <div class="field"><label>Stile di ballo</label><select id="fStile">${STILI_BALLO.map(s => `<option value="${s}" ${c.stile === s ? "selected" : ""}>${s}</option>`).join("")}</select></div>
    </div>
    <div class="row2">
      <div class="field"><label>Livello</label><select id="fLivello">${LIVELLI.map(l => `<option value="${l}" ${c.livello === l ? "selected" : ""}>${l}</option>`).join("")}</select></div>
      <div class="field"><label>Istruttore</label><select id="fIstruttore"><option value="">-- nessuno --</option>${istruttoriLookup.map(i => `<option value="${i.id}" ${c.istruttoreId === i.id ? "selected" : ""}>${escapeHtml(i.nome)} ${escapeHtml(i.cognome)}</option>`).join("")}</select></div>
    </div>
    <div class="row2">
      <div class="field"><label>Sala</label><select id="fSala"><option value="">-- nessuna --</option>${saleLookup.map(s => `<option value="${s.id}" ${c.salaId === s.id ? "selected" : ""}>${escapeHtml(s.nome)}</option>`).join("")}</select></div>
      <div class="field"><label>Capienza massima</label><input id="fCapienza" type="number" value="${c.capienzaMax ?? ""}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Orario inizio</label><input id="fOrarioInizio" type="time" value="${c.orarioInizio || ""}"></div>
      <div class="field"><label>Orario fine</label><input id="fOrarioFine" type="time" value="${c.orarioFine || ""}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Prezzo mensile (€)</label><input id="fPrezzo" type="number" step="0.01" value="${c.prezzoMensile ?? ""}"></div>
      <div class="field"><label>Data inizio</label><input id="fDataInizio" type="date" value="${c.dataInizio || ""}"></div>
    </div>
    <div class="field"><label>Giorni della settimana</label>
      <div class="checkbox-list">
        ${GIORNI.map(g => `<label><input type="checkbox" value="${g}" class="fGiorno" ${giorni.has(g) ? "checked" : ""}> ${GIORNI_LABEL[g]}</label>`).join("")}
      </div>
    </div>
    <div class="field"><label><input type="checkbox" id="fAttivo" ${c.attivo ? "checked" : ""} style="width:auto;display:inline-block"> Corso attivo</label></div>
  `;
}

function readCorsoForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    stile: document.getElementById("fStile").value,
    livello: document.getElementById("fLivello").value,
    istruttoreId: document.getElementById("fIstruttore").value ? parseInt(document.getElementById("fIstruttore").value) : null,
    salaId: document.getElementById("fSala").value ? parseInt(document.getElementById("fSala").value) : null,
    capienzaMax: document.getElementById("fCapienza").value ? parseInt(document.getElementById("fCapienza").value) : null,
    orarioInizio: document.getElementById("fOrarioInizio").value || null,
    orarioFine: document.getElementById("fOrarioFine").value || null,
    prezzoMensile: document.getElementById("fPrezzo").value ? parseFloat(document.getElementById("fPrezzo").value) : null,
    dataInizio: document.getElementById("fDataInizio").value || null,
    giorniSettimana: Array.from(document.querySelectorAll(".fGiorno:checked")).map(el => el.value),
    attivo: document.getElementById("fAttivo").checked
  };
}

function nuovoCorso() {
  openModal("Nuovo corso", corsoForm(), async () => {
    const dto = readCorsoForm();
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.post("/api/corsi", dto);
      await loadCorsi();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaCorso(id) {
  const c = corsiCache.find(x => x.id === id);
  openModal("Modifica corso", corsoForm(c), async () => {
    const dto = readCorsoForm();
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.put(`/api/corsi/${id}`, dto);
      await loadCorsi();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaCorso(id) {
  if (!confirm("Eliminare questo corso?")) return;
  try {
    await api.del(`/api/corsi/${id}`);
    await loadCorsi();
  } catch (err) { showError(err.message); }
}

loadCorsi();
