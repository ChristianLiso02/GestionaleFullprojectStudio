let abbonamentiCache = [];

async function loadAbbonamenti() {
  try {
    abbonamentiCache = await api.get("/api/abbonamenti");
    renderAbbonamenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderAbbonamenti() {
  const body = document.getElementById("abbonamentiBody");
  body.innerHTML = abbonamentiCache.length === 0
    ? `<tr><td colspan="7" class="empty-state">Nessun tipo di abbonamento registrato.</td></tr>`
    : abbonamentiCache.map(a => `
        <tr>
          <td><b>${escapeHtml(a.nome)}</b></td>
          <td>${escapeHtml(a.descrizione)}</td>
          <td>${a.durataGiorni ?? "-"}</td>
          <td>${a.numeroLezioni ?? "-"}</td>
          <td>${formatEuro(a.prezzo)}</td>
          <td><span class="pill ${a.attivo ? "pill-success" : "pill-muted"}">${a.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaAbbonamento(${a.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaAbbonamento(${a.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function abbonamentoForm(a) {
  a = a || { nome: "", descrizione: "", durataGiorni: "", numeroLezioni: "", prezzo: "", attivo: true };
  return `
    <div class="row2">
      <div class="field"><label>Nome</label><input id="fNome" value="${escapeHtml(a.nome)}"></div>
      <div class="field"><label>Prezzo (€)</label><input id="fPrezzo" type="number" step="0.01" value="${a.prezzo ?? ""}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Durata (giorni)</label><input id="fDurata" type="number" value="${a.durataGiorni ?? ""}" placeholder="es. 30"></div>
      <div class="field"><label>Numero lezioni</label><input id="fLezioni" type="number" value="${a.numeroLezioni ?? ""}" placeholder="es. 10"></div>
    </div>
    <div class="field"><label>Descrizione</label><textarea id="fDescrizione" rows="2">${escapeHtml(a.descrizione)}</textarea></div>
    <div class="field"><label><input type="checkbox" id="fAttivo" ${a.attivo ? "checked" : ""} style="width:auto;display:inline-block"> Attivo</label></div>
  `;
}

function readAbbonamentoForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    prezzo: parseFloat(document.getElementById("fPrezzo").value) || 0,
    durataGiorni: document.getElementById("fDurata").value ? parseInt(document.getElementById("fDurata").value) : null,
    numeroLezioni: document.getElementById("fLezioni").value ? parseInt(document.getElementById("fLezioni").value) : null,
    descrizione: document.getElementById("fDescrizione").value.trim(),
    attivo: document.getElementById("fAttivo").checked
  };
}

function nuovoAbbonamento() {
  openModal("Nuovo abbonamento", abbonamentoForm(), async () => {
    const dto = readAbbonamentoForm();
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.post("/api/abbonamenti", dto);
      await loadAbbonamenti();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaAbbonamento(id) {
  const a = abbonamentiCache.find(x => x.id === id);
  openModal("Modifica abbonamento", abbonamentoForm(a), async () => {
    const dto = readAbbonamentoForm();
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.put(`/api/abbonamenti/${id}`, dto);
      await loadAbbonamenti();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaAbbonamento(id) {
  if (!confirm("Eliminare questo tipo di abbonamento?")) return;
  try {
    await api.del(`/api/abbonamenti/${id}`);
    await loadAbbonamenti();
  } catch (err) { showError(err.message); }
}

loadAbbonamenti();
