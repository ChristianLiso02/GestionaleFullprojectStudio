let iscrizioniCache = [];

async function loadIscrizioni() {
  try {
    iscrizioniCache = await api.get("/api/iscrizioni");
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
            <a class="btn btn-ghost btn-sm" href="iscrizione-form.html?id=${i.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaIscrizione(${i.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaIscrizione(id) {
  if (!confirm("Eliminare questa iscrizione?")) return;
  try {
    await api.del(`/api/iscrizioni/${id}`);
    await loadIscrizioni();
  } catch (err) { showError(err.message); }
}

loadIscrizioni();
