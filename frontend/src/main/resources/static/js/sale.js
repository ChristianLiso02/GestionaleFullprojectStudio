let saleCache = [];

async function loadSale() {
  try {
    saleCache = await api.get("/api/sale");
    renderSale();
  } catch (err) {
    showError(err.message);
  }
}

function renderSale() {
  const body = document.getElementById("saleBody");
  body.innerHTML = saleCache.length === 0
    ? `<tr><td colspan="4" class="empty-state">Nessuna sala registrata.</td></tr>`
    : saleCache.map(s => `
        <tr>
          <td><b>${escapeHtml(s.nome)}</b></td>
          <td>${s.capienza ?? "-"}</td>
          <td>${escapeHtml(s.note)}</td>
          <td class="cell-actions">
            <button class="btn btn-ghost btn-sm" onclick="modificaSala(${s.id})">Modifica</button>
            <button class="btn btn-ghost btn-sm" onclick="eliminaSala(${s.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

function salaForm(s) {
  s = s || { nome: "", capienza: "", note: "" };
  return `
    <div class="field"><label>Nome</label><input id="fNome" value="${escapeHtml(s.nome)}"></div>
    <div class="field"><label>Capienza</label><input id="fCapienza" type="number" value="${s.capienza ?? ""}"></div>
    <div class="field"><label>Note</label><textarea id="fNote" rows="3">${escapeHtml(s.note)}</textarea></div>
  `;
}

function nuovaSala() {
  openModal("Nuova sala", salaForm(), async () => {
    const dto = {
      nome: document.getElementById("fNome").value.trim(),
      capienza: document.getElementById("fCapienza").value ? parseInt(document.getElementById("fCapienza").value) : null,
      note: document.getElementById("fNote").value.trim()
    };
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.post("/api/sale", dto);
      await loadSale();
    } catch (err) { showError(err.message); return false; }
  });
}

function modificaSala(id) {
  const s = saleCache.find(x => x.id === id);
  openModal("Modifica sala", salaForm(s), async () => {
    const dto = {
      nome: document.getElementById("fNome").value.trim(),
      capienza: document.getElementById("fCapienza").value ? parseInt(document.getElementById("fCapienza").value) : null,
      note: document.getElementById("fNote").value.trim()
    };
    if (!dto.nome) { showError("Il nome è obbligatorio."); return false; }
    try {
      await api.put(`/api/sale/${id}`, dto);
      await loadSale();
    } catch (err) { showError(err.message); return false; }
  });
}

async function eliminaSala(id) {
  if (!confirm("Eliminare questa sala?")) return;
  try {
    await api.del(`/api/sale/${id}`);
    await loadSale();
  } catch (err) { showError(err.message); }
}

loadSale();
