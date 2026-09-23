(async function () {
  try {
    const stats = await api.get("/api/dashboard/stats");

    if (stats.stagioneCorrenteNome) {
      const sub = document.querySelector("#topbar-container .sub");
      if (sub) sub.textContent = `Panoramica della scuola — Stagione ${stats.stagioneCorrenteNome}`;
    }

    const cards = [
      { num: stats.studentiAttivi, lbl: "Studenti attivi" },
      { num: stats.corsiAttivi, lbl: "Corsi attivi" },
      { num: stats.iscrizioniAttive, lbl: "Iscrizioni attive" },
      { num: formatEuro(stats.incassiMeseCorrente), lbl: "Incassi mese corrente", accent: true },
      { num: stats.quoteNonPagateMese, lbl: "Quote non pagate (mese)", href: "quote.html", allarme: stats.quoteNonPagateMese > 0 },
      { num: stats.pagamentiInSospeso, lbl: "Pagamenti in sospeso" }
    ];
    document.getElementById("statGrid").innerHTML = cards.map(c => {
      const classi = `stat${c.accent ? " accent" : ""}${c.allarme ? " allarme" : ""}`;
      const contenuto = `<div class="num">${c.num}</div><div class="lbl">${c.lbl}</div>`;
      return c.href ? `<a class="${classi}" href="${c.href}">${contenuto}</a>` : `<div class="${classi}">${contenuto}</div>`;
    }).join("");

    const body = document.getElementById("scadenzeBody");
    const rows = stats.iscrizioniInScadenza || [];
    body.innerHTML = rows.length === 0
      ? `<tr><td colspan="4" class="empty-state">Nessuna iscrizione in scadenza nei prossimi 30 giorni.</td></tr>`
      : rows.map(i => `
          <tr>
            <td>${escapeHtml(i.studenteNomeCompleto)}</td>
            <td>${escapeHtml(i.corsoNome)}</td>
            <td>${formatDate(i.dataScadenza)}</td>
            <td><span class="pill ${pillClass(i.stato)}">${i.stato}</span></td>
          </tr>
        `).join("");
  } catch (err) {
    showError(err.message);
  }
})();

async function generaBackup() {
  const btn = document.getElementById("backupBtn");
  const status = document.getElementById("backupStatus");
  btn.disabled = true;
  btn.textContent = "Generazione in corso...";
  status.hidden = true;
  try {
    const result = await api.post("/api/backup/genera");
    status.textContent = "Backup generato: " + result.percorsoFile;
    status.style.color = "var(--verde)";
    status.hidden = false;
  } catch (err) {
    status.textContent = "Errore: " + err.message;
    status.style.color = "var(--rosso)";
    status.hidden = false;
  } finally {
    btn.disabled = false;
    btn.textContent = "Genera backup ora";
  }
}
