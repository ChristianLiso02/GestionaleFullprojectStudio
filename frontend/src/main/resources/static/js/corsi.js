const GIORNI_LABEL = { MONDAY: "Lun", TUESDAY: "Mar", WEDNESDAY: "Mer", THURSDAY: "Gio", FRIDAY: "Ven", SATURDAY: "Sab", SUNDAY: "Dom" };

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

function renderCorsi() {
  const body = document.getElementById("corsiBody");
  body.innerHTML = corsiCache.length === 0
    ? `<tr><td colspan="10" class="empty-state">Nessun corso registrato.</td></tr>`
    : corsiCache.map(c => `
        <tr>
          <td><a href="corso-dettaglio.html?id=${c.id}"><b>${escapeHtml(c.nome)}</b></a></td>
          <td>${c.stile}</td>
          <td>${c.livello}</td>
          <td>${escapeHtml((c.istruttoriNomi || []).join(" + ")) || "-"}</td>
          <td>${escapeHtml(c.salaNome) || "-"}</td>
          <td>${(c.giorniSettimana || []).map(g => GIORNI_LABEL[g]).join(" · ")}<br>${c.orarioInizio || ""} - ${c.orarioFine || ""}</td>
          <td>${formatPosti(c.iscrittiAttivi, c.capienzaMax)}</td>
          <td>${c.prezzoMensile != null ? formatEuro(c.prezzoMensile) : "-"}</td>
          <td><span class="pill ${c.attivo ? "pill-success" : "pill-muted"}">${c.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="corso-dettaglio.html?id=${c.id}">Iscritti</a>
            <a class="btn btn-ghost btn-sm" href="corso-form.html?id=${c.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaCorso(${c.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaCorso(id) {
  if (!confirm("Eliminare questo corso?")) return;
  try {
    await api.del(`/api/corsi/${id}`);
    await loadCorsi();
  } catch (err) { showError(err.message); }
}

initSelettoreStagione("stagioneSelect", (id) => {
  stagioneSelezionata = id;
  loadCorsi();
});
