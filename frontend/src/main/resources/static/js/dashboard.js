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
      { num: stats.quoteScadute, lbl: "Quote scadute da verificare", href: "#quoteScaduteBody", allarme: stats.quoteScadute > 0 },
      { num: stats.pagamentiInSospeso, lbl: "Pagamenti in sospeso" }
    ];
    document.getElementById("statGrid").innerHTML = cards.map(c => {
      const classi = `stat${c.accent ? " accent" : ""}${c.allarme ? " allarme" : ""}`;
      const contenuto = `<div class="num">${c.num}</div><div class="lbl">${c.lbl}</div>`;
      return c.href ? `<a class="${classi}" href="${c.href}">${contenuto}</a>` : `<div class="${classi}">${contenuto}</div>`;
    }).join("");

    const body = document.getElementById("quoteScaduteBody");
    const rows = stats.quoteScaduteDaVerificare || [];
    body.innerHTML = rows.length === 0
      ? `<tr><td colspan="5" class="empty-state">Nessuna quota scaduta: tutti in regola.</td></tr>`
      : rows.map(r => `
          <tr>
            <td><a href="studente-dettaglio.html?id=${r.studenteId}"><b>${escapeHtml(r.studenteNomeCompleto)}</b></a></td>
            <td>${escapeHtml(r.corsoNome)}<br><span class="testo-tenue">${escapeHtml(r.tipoAbbonamentoNome) || "-"} · ${formatEuro(r.quotaImporto)}</span></td>
            <td><span class="pill pill-danger">${r.mesiScaduti.length} ${r.mesiScaduti.length === 1 ? "mese" : "mesi"}</span><br><span class="testo-tenue">${r.mesiScaduti.map(formatMese).join(", ")}</span></td>
            <td>${r.ultimoPagamento ? formatDate(r.ultimoPagamento) : "mai"}</td>
            <td class="cell-actions">
              <a class="btn btn-accent btn-sm" href="pagamento-form.html?studenteId=${r.studenteId}&iscrizioneId=${r.iscrizioneId}&mese=${r.mesiScaduti[0]}">Registra pagamento</a>
              <button class="btn btn-ghost btn-sm" onclick="ritiraIscrizione(${r.iscrizioneId}, '${r.dataRitiroProposta}')">Segna ritirato</button>
            </td>
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
