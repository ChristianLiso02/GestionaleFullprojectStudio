const METODI_PAGAMENTO = ["CONTANTI", "CARTA", "BONIFICO", "ALTRO"];
const STATI_PAGAMENTO = ["PAGATO", "IN_SOSPESO", "RIMBORSATO"];

let pagamentiCache = [];
let studentiLookupPag = [];
let iscrizioniLookupPag = [];

async function loadPagamenti() {
  try {
    [pagamentiCache, studentiLookupPag, iscrizioniLookupPag] = await Promise.all([
      api.get("/api/pagamenti"),
      api.get("/api/studenti"),
      api.get("/api/iscrizioni")
    ]);
    renderPagamenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderPagamenti() {
  const body = document.getElementById("pagamentiBody");
  const rows = [...pagamentiCache].sort((a, b) => (a.dataPagamento < b.dataPagamento ? 1 : -1));
  body.innerHTML = rows.length === 0
    ? `<tr><td colspan="7" class="empty-state">Nessun pagamento registrato.</td></tr>`
    : rows.map(p => `
        <tr>
          <td><b>${escapeHtml(p.studenteNomeCompleto)}</b></td>
          <td>${formatDate(p.dataPagamento)}</td>
          <td>${formatEuro(p.importo)}</td>
          <td>${p.metodo}</td>
          <td>${escapeHtml(p.causale)}</td>
          <td><span class="pill ${pillClass(p.stato)}">${p.stato}</span></td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaPagamento(${p.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaPagamento(${p.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function pagamentoForm(p) {
  p = p || { studenteId: "", iscrizioneId: "", importo: "", dataPagamento: todayISO(), metodo: "CONTANTI", causale: "", stato: "PAGATO", note: "" };
  return `
    <div class="field"><label>Studente</label><select id="fStudente">
      <option value="">-- seleziona --</option>
      ${studentiLookupPag.map(s => `<option value="${s.id}" ${p.studenteId === s.id ? "selected" : ""}>${escapeHtml(s.nome)} ${escapeHtml(s.cognome)}</option>`).join("")}
    </select></div>
    <div class="field"><label>Iscrizione collegata</label><select id="fIscrizione">
      <option value="">-- nessuna --</option>
      ${iscrizioniLookupPag.map(i => `<option value="${i.id}" ${p.iscrizioneId === i.id ? "selected" : ""}>${escapeHtml(i.studenteNomeCompleto)} - ${escapeHtml(i.corsoNome)}</option>`).join("")}
    </select></div>
    <div class="row2">
      <div class="field"><label>Importo (€)</label><input id="fImporto" type="number" step="0.01" value="${p.importo ?? ""}"></div>
      <div class="field"><label>Data pagamento</label><input id="fData" type="date" value="${p.dataPagamento || todayISO()}"></div>
    </div>
    <div class="row2">
      <div class="field"><label>Metodo</label><select id="fMetodo">${METODI_PAGAMENTO.map(m => `<option value="${m}" ${p.metodo === m ? "selected" : ""}>${m}</option>`).join("")}</select></div>
      <div class="field"><label>Stato</label><select id="fStato">${STATI_PAGAMENTO.map(s => `<option value="${s}" ${p.stato === s ? "selected" : ""}>${s}</option>`).join("")}</select></div>
    </div>
    <div class="field"><label>Causale</label><input id="fCausale" value="${escapeHtml(p.causale)}" placeholder="es. Quota mensile Salsa Cubana"></div>
    <div class="field"><label>Note</label><textarea id="fNote" rows="2">${escapeHtml(p.note)}</textarea></div>
  `;
}

function readPagamentoForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    iscrizioneId: document.getElementById("fIscrizione").value ? parseInt(document.getElementById("fIscrizione").value) : null,
    importo: parseFloat(document.getElementById("fImporto").value) || 0,
    dataPagamento: document.getElementById("fData").value || todayISO(),
    metodo: document.getElementById("fMetodo").value,
    stato: document.getElementById("fStato").value,
    causale: document.getElementById("fCausale").value.trim(),
    note: document.getElementById("fNote").value.trim()
  };
}

function nuovoPagamento() {
  openModal("Nuovo pagamento", pagamentoForm(), async () => {
    const dto = readPagamentoForm();
    if (!dto.studenteId) { showError("Lo studente è obbligatorio."); return false; }
    try {
      await api.post("/api/pagamenti", dto);
      await loadPagamenti();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaPagamento(id) {
  const p = pagamentiCache.find(x => x.id === id);
  openModal("Modifica pagamento", pagamentoForm(p), async () => {
    const dto = readPagamentoForm();
    if (!dto.studenteId) { showError("Lo studente è obbligatorio."); return false; }
    try {
      await api.put(`/api/pagamenti/${id}`, dto);
      await loadPagamenti();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaPagamento(id) {
  if (!confirm("Eliminare questo pagamento?")) return;
  try {
    await api.del(`/api/pagamenti/${id}`);
    await loadPagamenti();
  } catch (err) { showError(err.message); }
}

loadPagamenti();
