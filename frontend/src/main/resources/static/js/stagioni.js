let stagioniCache = [];

async function loadStagioni() {
  try {
    stagioniCache = await api.get("/api/stagioni");
    renderStagioni();
  } catch (err) {
    showError(err.message);
  }
}

function renderStagioni() {
  const body = document.getElementById("stagioniBody");
  body.innerHTML = stagioniCache.length === 0
    ? `<tr><td colspan="4" class="empty-state">Nessuna stagione creata.</td></tr>`
    : stagioniCache.map(s => `
        <tr>
          <td><b>${escapeHtml(s.nome)}</b></td>
          <td>${s.dataInizio ? formatDate(s.dataInizio) : "-"} &ndash; ${s.dataFine ? formatDate(s.dataFine) : "-"}</td>
          <td><span class="pill ${s.corrente ? "pill-success" : "pill-muted"}">${s.corrente ? "Corrente" : "Archiviata"}</span></td>
          <td class="cell-actions">
            ${s.corrente ? "" : `<button class="btn btn-ghost btn-sm" onclick="rendiCorrente(${s.id})">Rendi corrente</button>`}
            <a class="btn btn-ghost btn-sm" href="stagione-form.html?id=${s.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaStagione(${s.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function rendiCorrente(id) {
  if (!confirm("Rendere questa la stagione corrente? Le pagine Corsi, Iscrizioni e Dashboard mostreranno di default i dati di questa stagione.")) return;
  try {
    await api.put(`/api/stagioni/${id}/corrente`);
    await loadStagioni();
  } catch (err) { showError(err.message); }
}

async function eliminaStagione(id) {
  if (!confirm("Eliminare questa stagione? Possibile solo se non ha corsi collegati.")) return;
  try {
    await api.del(`/api/stagioni/${id}`);
    await loadStagioni();
  } catch (err) { showError(err.message); }
}

loadStagioni();
