let dati = null;
let vista = "scuola";
let meseScelto = null;

function mesiVista() {
  if (vista === "scuola") return dati.scuola;
  const corso = dati.corsi.find(c => String(c.corsoId) === vista);
  return corso ? corso.mesi : dati.scuola;
}

async function caricaStatistiche(stagioneId) {
  try {
    dati = await api.get(`/api/statistiche${stagioneId ? `?stagioneId=${stagioneId}` : ""}`);
    const vistaSelect = document.getElementById("vistaSelect");
    vistaSelect.innerHTML = `<option value="scuola">Tutta la scuola</option>` +
      dati.corsi.map(c => `<option value="${c.corsoId}">${escapeHtml(c.corsoNome)}</option>`).join("");
    if (!dati.corsi.some(c => String(c.corsoId) === vista)) vista = "scuola";
    vistaSelect.value = vista;

    const meseSelect = document.getElementById("meseSelect");
    const mesi = dati.scuola.map(m => m.mese);
    meseSelect.innerHTML = [...mesi].reverse().map(m => `<option value="${m}">${formatMese(m)}</option>`).join("");
    if (!mesi.includes(meseScelto)) meseScelto = mesi[mesi.length - 1];
    meseSelect.value = meseScelto;

    render();
  } catch (err) {
    showError(err.message);
  }
}

function render() {
  const mesi = mesiVista();
  const nomeVista = vista === "scuola" ? "Tutta la scuola" : document.getElementById("vistaSelect").selectedOptions[0].textContent;
  document.getElementById("titoloStatistiche").textContent = `Statistiche — ${nomeVista} · Stagione ${dati.stagioneNome}`;
  renderKpiStatistiche(mesi, meseScelto, vista === "scuola");
  renderGraficiStatistiche(mesi, meseScelto, vista === "scuola");
  renderTabellaMesi(mesi);
  renderConfrontoCorsi();
}

function renderTabellaMesi(mesi) {
  const scuola = vista === "scuola";
  document.getElementById("mesiHead").innerHTML = `<tr>
    <th>Mese</th><th>Iscritti attivi</th>${scuola ? "<th>Studenti distinti</th>" : ""}<th>Uomini</th><th>Donne</th>
    <th>Nuove</th><th>Ritiri</th><th>Rientri</th><th>Incassi</th>
    <th>In tempo</th><th>In ritardo</th><th>Scadute</th><th>Da rinnovare</th><th>Puntualità</th></tr>`;
  const riga = (m, classe = "") => `
    <tr class="${classe}">
      <td>${m.mese ? `<b>${formatMese(m.mese)}</b>` : "<b>Totale stagione</b>"}</td>
      <td>${m.iscrittiAttivi ?? "–"}</td>${scuola ? `<td>${m.studentiAttivi ?? "–"}</td>` : ""}
      <td>${m.uomini ?? "–"}</td><td>${m.donne ?? "–"}</td>
      <td>${m.nuoveIscrizioni}</td><td>${m.ritiri}</td><td>${m.rientri}</td>
      <td>${formatEuro(m.incassi)}</td>
      <td>${m.quotePagateInTempo}</td><td>${m.quotePagateInRitardo}</td><td>${m.quoteScadute}</td><td>${m.quoteDaRinnovare}</td>
      <td>${formatPercentuale(puntualita(m.quotePagateInTempo, m.quotePagateInRitardo, m.quoteScadute))}</td>
    </tr>`;
  const totali = {
    nuoveIscrizioni: somma(mesi, "nuoveIscrizioni"), ritiri: somma(mesi, "ritiri"), rientri: somma(mesi, "rientri"),
    incassi: somma(mesi, "incassi"), quotePagateInTempo: somma(mesi, "quotePagateInTempo"),
    quotePagateInRitardo: somma(mesi, "quotePagateInRitardo"), quoteScadute: somma(mesi, "quoteScadute"),
    quoteDaRinnovare: somma(mesi, "quoteDaRinnovare")
  };
  document.getElementById("mesiBody").innerHTML =
    [...mesi].reverse().map(m => riga(m, m.mese === meseScelto ? "riga-scelta" : "")).join("") + riga(totali, "riga-totale");
}

function renderConfrontoCorsi() {
  const pannello = document.getElementById("pannelloCorsi");
  pannello.hidden = vista !== "scuola";
  if (pannello.hidden) return;
  document.getElementById("titoloConfronto").textContent = `Confronto corsi · ${formatMese(meseScelto)}`;
  const body = document.getElementById("corsiBody");
  body.innerHTML = dati.corsi.length === 0
    ? `<tr><td colspan="9" class="empty-state">Nessun corso in questa stagione.</td></tr>`
    : dati.corsi.map(c => {
        const m = c.mesi.find(x => x.mese === meseScelto) || {};
        const punt = puntualita(somma(c.mesi, "quotePagateInTempo"), somma(c.mesi, "quotePagateInRitardo"), somma(c.mesi, "quoteScadute"));
        return `
          <tr>
            <td><b>${escapeHtml(c.corsoNome)}</b>${c.attivo ? "" : `<br><span class="testo-tenue">corso non attivo</span>`}</td>
            <td>${formatPosti(m.iscrittiAttivi || 0, c.capienzaMax)}</td>
            <td>${formatUominiDonne(m.uomini, m.donne, m.iscrittiAttivi)}</td>
            <td>${m.quoteScadute ? `<span class="pill pill-danger">${m.quoteScadute}</span>` : "0"}</td>
            <td>${somma(c.mesi, "nuoveIscrizioni")}</td>
            <td>${somma(c.mesi, "ritiri")}</td>
            <td>${formatEuro(somma(c.mesi, "incassi"))}</td>
            <td>${formatPercentuale(punt)}</td>
            <td class="cell-actions"><button class="btn btn-ghost btn-sm" onclick="scegliVista('${c.corsoId}')">Dettaglio</button></td>
          </tr>`;
      }).join("");
}

function scegliVista(valore) {
  vista = String(valore);
  document.getElementById("vistaSelect").value = vista;
  render();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

document.getElementById("vistaSelect").addEventListener("change", e => scegliVista(e.target.value));
document.getElementById("meseSelect").addEventListener("change", e => { meseScelto = e.target.value; render(); });
// Aperta dal dettaglio corso: statistiche.html?stagione=ID&corso=ID
const parametri = new URLSearchParams(window.location.search);
if (parametri.get("corso")) vista = parametri.get("corso");
let stagioneRichiesta = parametri.get("stagione");
initSelettoreStagione("stagioneSelect", id => {
  if (stagioneRichiesta && stagioneRichiesta !== String(id)) {
    document.getElementById("stagioneSelect").value = stagioneRichiesta;
    id = parseInt(stagioneRichiesta);
  }
  stagioneRichiesta = null;
  caricaStatistiche(id);
});
