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
    ? `<tr><td colspan="5" class="empty-state">Nessuna iscrizione in questa stagione.</td></tr>`
    : iscrizioniCache.map(i => `
        <tr ${attributiRiga(`iscrizione-dettaglio.html?id=${i.id}`)}>
          <td>
            <div class="cella-entita">
              <span class="entita-icona studente">${icona("studenti")}</span>
              <b>${escapeHtml(i.studenteNomeCompleto)}</b>
            </div>
          </td>
          <td>
            <div class="cella-entita">
              <span class="entita-icona corso">${icona("corsi")}</span>
              <div><b>${escapeHtml(i.corsoNome)}</b><div class="corsi-sub">${escapeHtml(i.tipoAbbonamentoNome) || "Nessun abbonamento"}</div></div>
            </div>
          </td>
          <td>${formatDate(i.dataIscrizione)}</td>
          <td>${statoIscrizioneHtml(i)}</td>
          <td class="cell-actions">
            ${azioneStatoIscrizione(i)}
            <a class="btn btn-ghost btn-sm" href="iscrizione-form.html?id=${i.id}">Modifica</a>
            <button class="btn btn-elimina btn-sm" onclick="eliminaIscrizione(${i.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaIscrizione(id) {
  if (!await confermaEliminazione("questa iscrizione", "Se lo studente ha smesso di venire, usa invece \"Ritira\": così restano storico e pagamenti.")) return;
  try {
    await api.del(`/api/iscrizioni/${id}`);
    await loadIscrizioni();
  } catch (err) { showError(err.message); }
}

initSelettoreStagione("stagioneSelect", (id) => {
  stagioneSelezionata = id;
  loadIscrizioni();
});
