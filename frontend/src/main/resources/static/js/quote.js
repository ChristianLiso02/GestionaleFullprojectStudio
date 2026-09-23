const STATI_QUOTA = {
  NON_PAGATO: { label: "Non pagato", plurale: "Non pagati", pill: "pill-danger" },
  DA_PAGARE: { label: "Da pagare", plurale: "Da pagare", pill: "pill-muted" },
  PAGATO_IN_RITARDO: { label: "Pagato in ritardo", plurale: "Pagati in ritardo", pill: "pill-warning" },
  PAGATO_IN_TEMPO: { label: "Pagato in tempo", plurale: "Pagati in tempo", pill: "pill-success" }
};

let meseSelezionato = new URLSearchParams(window.location.search).get("mese") || todayISO().slice(0, 7);
let stagioneSelezionata = null;
let righe = [];
let filtroStato = null;

async function loadQuote() {
  try {
    const stagione = stagioneSelezionata ? `&stagioneId=${stagioneSelezionata}` : "";
    const dati = await api.get(`/api/pagamenti/quote?mese=${meseSelezionato}${stagione}`);
    righe = dati.righe;
    document.getElementById("titoloMese").textContent = `Quote di ${formatMese(meseSelezionato)}`;
    document.getElementById("notaScadenza").textContent =
      `La quota va pagata entro il giorno ${dati.giornoScadenza} del mese. ` +
      `Chi si iscrive dopo il ${dati.giornoScadenza} paga al momento dell'iscrizione. ` +
      `Il trimestrale copre 3 mesi a partire dal mese di riferimento del pagamento.`;
    render();
  } catch (err) {
    showError(err.message);
  }
}

function render() {
  const conteggi = Object.fromEntries(Object.keys(STATI_QUOTA).map(s => [s, 0]));
  righe.forEach(r => { conteggi[r.stato]++; });

  document.getElementById("riepilogoQuote").innerHTML = Object.entries(STATI_QUOTA).map(([stato, info]) => `
    <button type="button" class="chip-quota${filtroStato === stato ? " attivo" : ""}" onclick="filtra('${stato}')">
      <span class="pill ${info.pill}">${info.plurale}</span><b>${conteggi[stato]}</b>
    </button>
  `).join("");

  const visibili = filtroStato ? righe.filter(r => r.stato === filtroStato) : righe;
  const body = document.getElementById("quoteBody");
  body.innerHTML = visibili.length === 0
    ? `<tr><td colspan="7" class="empty-state">${righe.length === 0 ? "Nessuna iscrizione attiva in questo mese." : "Nessuna quota in questo stato."}</td></tr>`
    : visibili.map(r => {
        const info = STATI_QUOTA[r.stato];
        const pagata = r.pagamentoId != null;
        const copertura = pagata && r.coperturaFino && r.coperturaFino !== meseSelezionato
          ? `<br><span class="testo-tenue">copre fino a ${formatMese(r.coperturaFino)}</span>` : "";
        const azione = pagata
          ? `<a class="btn btn-ghost btn-sm" href="pagamento-form.html?id=${r.pagamentoId}&mese=${meseSelezionato}">Vedi pagamento</a>`
          : `<a class="btn btn-accent btn-sm" href="pagamento-form.html?studenteId=${r.studenteId}&iscrizioneId=${r.iscrizioneId}&mese=${meseSelezionato}">Registra pagamento</a>`;
        return `
          <tr>
            <td><a href="studente-dettaglio.html?id=${r.studenteId}"><b>${escapeHtml(r.studenteNomeCompleto)}</b></a></td>
            <td><a href="corso-dettaglio.html?id=${r.corsoId}">${escapeHtml(r.corsoNome)}</a></td>
            <td>${escapeHtml(r.tipoAbbonamentoNome) || "-"}<br><span class="testo-tenue">${formatEuro(r.quotaImporto)}</span></td>
            <td>${formatDate(r.scadenza)}</td>
            <td><span class="pill ${info.pill}">${info.label}</span></td>
            <td>${pagata ? formatDate(r.dataPagamento) : "-"}${copertura}</td>
            <td class="cell-actions">${azione}</td>
          </tr>
        `;
      }).join("");
}

function filtra(stato) {
  filtroStato = filtroStato === stato ? null : stato;
  render();
}

const meseInput = document.getElementById("meseInput");
meseInput.value = meseSelezionato;
meseInput.addEventListener("change", () => {
  if (!meseInput.value) return;
  meseSelezionato = meseInput.value;
  loadQuote();
});

initSelettoreStagione("stagioneSelect", (id) => {
  stagioneSelezionata = id;
  loadQuote();
});
