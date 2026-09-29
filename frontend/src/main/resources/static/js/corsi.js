let corsiCache = [];
let stagioneSelezionata = null;

async function loadCorsi() {
  try {
    corsiCache = stagioneSelezionata
      ? await api.get(`/api/corsi?stagioneId=${stagioneSelezionata}`)
      : await api.get("/api/corsi");
    renderCorsi();
  } catch (err) {
    showError(err.message);
  }
}

function renderRiepilogoSesso() {
  const box = document.getElementById("riepilogoSesso");
  const somma = (campo) => corsiCache.reduce((tot, c) => tot + (c[campo] || 0), 0);
  const totale = somma("iscrittiAttivi");
  box.hidden = totale === 0;
  box.innerHTML = `
    <b>Iscrizioni attive nei corsi elencati:</b>
    ${formatUominiDonne(somma("iscrittiUomini"), somma("iscrittiDonne"), totale)}
  `;
}

function renderCorsi() {
  renderRiepilogoSesso();
  const body = document.getElementById("corsiBody");
  body.innerHTML = corsiCache.length === 0
    ? `<tr><td colspan="8" class="empty-state">Nessun corso in questa stagione. Crea il primo con “+ Nuovo corso”.</td></tr>`
    : corsiCache.map(c => `
        <tr ${attributiRiga(`corso-dettaglio.html?id=${c.id}`)}>
          <td style="min-width:170px"><b>${escapeHtml(c.nome)}</b>${c.attivo ? "" : ' <span class="pill pill-muted">Non attivo</span>'}<div class="corsi-sub">${formatEnum(c.stile)} · ${formatEnum(c.livello)}</div></td>
          <td class="nowrap">${(c.istruttoriNomi || []).map(escapeHtml).join("<br>") || "-"}</td>
          <td class="nowrap">${escapeHtml(c.salaNome) || "-"}</td>
          <td style="white-space:nowrap">${formatGiorni(c.giorniSettimana) || "-"}<div class="corsi-sub">${formatFasciaOraria(c)}</div></td>
          <td>${formatPosti(c.iscrittiAttivi, c.capienzaMax)}</td>
          <td>${formatUominiDonne(c.iscrittiUomini, c.iscrittiDonne, c.iscrittiAttivi)}</td>
          <td style="white-space:nowrap">${c.prezzoMensile != null ? formatEuro(c.prezzoMensile) : "-"}</td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="corso-form.html?id=${c.id}">Modifica</a>
            <button class="btn btn-elimina btn-sm" onclick="eliminaCorso(${c.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaCorso(id) {
  if (!await confermaEliminazione("questo corso", "Possibile solo se non ha iscrizioni o pagamenti collegati.")) return;
  try {
    await api.del(`/api/corsi/${id}`);
    await loadCorsi();
  } catch (err) { showError(err.message); }
}

initSelettoreStagione("stagioneSelect", (id) => {
  stagioneSelezionata = id;
  loadCorsi();
});
