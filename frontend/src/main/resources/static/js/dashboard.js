(async function () {
  try {
    const stats = await api.get("/api/dashboard/stats");

    const cards = [
      { num: stats.studentiAttivi, lbl: "Studenti attivi" },
      { num: stats.corsiAttivi, lbl: "Corsi attivi" },
      { num: stats.iscrizioniAttive, lbl: "Iscrizioni attive" },
      { num: formatEuro(stats.incassiMeseCorrente), lbl: "Incassi mese corrente", accent: true },
      { num: stats.pagamentiInSospeso, lbl: "Pagamenti in sospeso" }
    ];
    document.getElementById("statGrid").innerHTML = cards.map(c => `
      <div class="stat${c.accent ? " accent" : ""}">
        <div class="num">${c.num}</div>
        <div class="lbl">${c.lbl}</div>
      </div>
    `).join("");

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
