let iscrizioniCache = [];
let stagioneSelezionata = null;

async function loadIscrizioni() {
  try {
    iscrizioniCache = stagioneSelezionata
      ? await api.get(`/api/iscrizioni?stagioneId=${stagioneSelezionata}`)
      : await api.get("/api/iscrizioni");
    renderIscrizioni();
  } catch (err) {
    showError(err.message);
  }
}

function renderIscrizioni() {
  const body = document.getElementById("iscrizioniBody");
  body.innerHTML = iscrizioniCache.length === 0
    ? `<tr><td colspan="6" class="empty-state">Nessuna iscrizione registrata.</td></tr>`
    : iscrizioniCache.map(i => `
        <tr>
          <td><b>${escapeHtml(i.studenteNomeCompleto)}</b></td>
          <td>${escapeHtml(i.corsoNome)}</td>
          <td>${escapeHtml(i.tipoAbbonamentoNome) || "-"}</td>
          <td>${formatDate(i.dataIscrizione)}</td>
          <td>${statoIscrizioneHtml(i)}</td>
          <td class="cell-actions">
            ${azioneStatoIscrizione(i)}
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

initSelettoreStagione("stagioneSelect", (id) => {
  stagioneSelezionata = id;
  loadIscrizioni();
});
