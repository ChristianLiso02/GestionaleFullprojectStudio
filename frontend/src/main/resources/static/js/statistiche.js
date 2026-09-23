// Colori verificati col validatore della palette (vedi grafici): nero/rosso del brand per le serie,
// colori di stato per le quote, coerenti con le etichette usate nel resto del gestionale.
const COLORI = {
  serie: "#141110",
  ritiri: "#c1121f",
  inTempo: "#1f7a4d",
  inRitardo: "#d4a017",
  scadute: "#c1121f",
  daRinnovare: "#2a78d6"
};

let dati = null;
let vista = "scuola";
let meseScelto = null;

const somma = (mesi, campo) => mesi.reduce((t, m) => t + Number(m[campo] || 0), 0);

function puntualita(inTempo, inRitardo, scadute) {
  const totale = inTempo + inRitardo + scadute;
  return totale === 0 ? null : Math.round((inTempo * 100) / totale);
}

function formatPercentuale(p) {
  return p == null ? "–" : `${p}%`;
}

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
  renderKpi(mesi);
  renderGrafici(mesi);
  renderTabellaMesi(mesi);
  renderConfrontoCorsi();
}

function delta(attuale, precedente, suEBene, formato = v => v) {
  if (precedente == null) return "";
  const differenza = attuale - precedente;
  if (differenza === 0) return `<div class="delta">= come il mese prima</div>`;
  const buono = (differenza > 0) === suEBene;
  const segno = differenza > 0 ? "▲ +" : "▼ −";
  return `<div class="delta ${buono ? "buono" : "cattivo"}">${segno}${formato(Math.abs(differenza))} rispetto al mese prima</div>`;
}

function renderKpi(mesi) {
  const i = mesi.findIndex(m => m.mese === meseScelto);
  const m = mesi[i];
  const p = i > 0 ? mesi[i - 1] : null;
  const scuola = vista === "scuola";
  const iscritti = scuola ? m.studentiAttivi : m.iscrittiAttivi;
  const iscrittiPrima = p ? (scuola ? p.studentiAttivi : p.iscrittiAttivi) : null;
  const punt = puntualita(m.quotePagateInTempo, m.quotePagateInRitardo, m.quoteScadute);

  const schede = [
    { valore: iscritti, etichetta: scuola ? "Studenti attivi" : "Iscritti attivi", extra: delta(iscritti, iscrittiPrima, true) +
        (scuola && m.iscrittiAttivi !== m.studentiAttivi ? `<div class="delta">${m.iscrittiAttivi} iscrizioni ai corsi</div>` : "") },
    { valore: formatEuroBreve(m.incassi), etichetta: "Incassi del mese", accent: true, extra: delta(Number(m.incassi), p ? Number(p.incassi) : null, true, formatEuroBreve) },
    { valore: m.nuoveIscrizioni, etichetta: "Nuove iscrizioni", extra: delta(m.nuoveIscrizioni, p ? p.nuoveIscrizioni : null, true) },
    { valore: m.ritiri, etichetta: "Ritiri", extra: delta(m.ritiri, p ? p.ritiri : null, false) + (m.rientri ? `<div class="delta">${m.rientri} rientrati</div>` : "") },
    { valore: formatPercentuale(punt), etichetta: "Quote pagate in tempo", extra: `<div class="delta">${m.quoteScadute} scadute · ${m.quoteDaRinnovare} da rinnovare</div>` },
    { valore: `${m.uomini} / ${m.donne}`, etichetta: "Uomini / Donne", extra: `<div style="margin-top:8px">${formatUominiDonne(m.uomini, m.donne, m.iscrittiAttivi)}</div>` }
  ];
  document.getElementById("kpiTitolo").textContent = `Situazione di ${formatMese(meseScelto)}`;
  document.getElementById("kpi").innerHTML = schede.map(s => `
    <div class="stat${s.accent ? " accent" : ""}">
      <div class="num">${s.valore}</div>
      <div class="lbl">${s.etichetta}</div>
      ${s.extra || ""}
    </div>
  `).join("");
}

function renderGrafici(mesi) {
  const etichette = mesi.map(m => m.mese);
  const valori = campo => mesi.map(m => Number(m[campo] || 0));
  const comune = { mesi: etichette, meseEvidenziato: meseScelto };

  disegnaGraficoColonne(document.getElementById("graficoIscritti"), {
    ...comune, etichettaUltimo: true,
    serie: [{ nome: vista === "scuola" ? "Iscrizioni attive" : "Iscritti attivi", colore: COLORI.serie, valori: valori("iscrittiAttivi") }]
  });
  disegnaGraficoColonne(document.getElementById("graficoIncassi"), {
    ...comune, etichettaUltimo: true, intero: false, formato: formatEuroBreve,
    serie: [{ nome: "Incassi", colore: COLORI.serie, valori: valori("incassi") }]
  });
  disegnaGraficoColonne(document.getElementById("graficoMovimenti"), {
    ...comune,
    serie: [
      { nome: "Nuove iscrizioni", colore: COLORI.serie, valori: valori("nuoveIscrizioni") },
      { nome: "Ritiri", colore: COLORI.ritiri, valori: valori("ritiri"), verso: -1 }
    ]
  });
  disegnaGraficoColonne(document.getElementById("graficoQuote"), {
    ...comune, impilato: true,
    serie: [
      { nome: "Pagate in tempo", colore: COLORI.inTempo, valori: valori("quotePagateInTempo") },
      { nome: "Pagate in ritardo", colore: COLORI.inRitardo, valori: valori("quotePagateInRitardo") },
      { nome: "Scadute", colore: COLORI.scadute, valori: valori("quoteScadute") },
      { nome: "Da rinnovare", colore: COLORI.daRinnovare, valori: valori("quoteDaRinnovare") }
    ]
  });
}

function formatEuroBreve(v) {
  return "€ " + Number(v).toLocaleString("it-IT", { maximumFractionDigits: 0 });
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
initSelettoreStagione("stagioneSelect", id => caricaStatistiche(id));
