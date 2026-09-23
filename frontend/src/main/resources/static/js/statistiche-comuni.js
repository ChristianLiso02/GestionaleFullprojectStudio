// Funzioni condivise tra la pagina Statistiche e il dettaglio corso.

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

const somma = (mesi, campo) => mesi.reduce((t, m) => t + Number(m[campo] || 0), 0);

function puntualita(inTempo, inRitardo, scadute) {
  const totale = inTempo + inRitardo + scadute;
  return totale === 0 ? null : Math.round((inTempo * 100) / totale);
}

function formatPercentuale(p) {
  return p == null ? "–" : `${p}%`;
}

function delta(attuale, precedente, suEBene, formato = v => v) {
  if (precedente == null) return "";
  const differenza = attuale - precedente;
  if (differenza === 0) return `<div class="delta">= come il mese prima</div>`;
  const buono = (differenza > 0) === suEBene;
  const segno = differenza > 0 ? "▲ +" : "▼ −";
  return `<div class="delta ${buono ? "buono" : "cattivo"}">${segno}${formato(Math.abs(differenza))} rispetto al mese prima</div>`;
}

// Riquadri di sintesi del mese scelto (id "kpi" e "kpiTitolo" nella pagina).
function renderKpiStatistiche(mesi, meseScelto, scuola) {
  const i = mesi.findIndex(m => m.mese === meseScelto);
  const m = mesi[i];
  const p = i > 0 ? mesi[i - 1] : null;
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

// I quattro grafici mese per mese (id "graficoIscritti", "graficoIncassi", "graficoMovimenti", "graficoQuote").
function renderGraficiStatistiche(mesi, meseScelto, scuola) {
  const etichette = mesi.map(m => m.mese);
  const valori = campo => mesi.map(m => Number(m[campo] || 0));
  const comune = { mesi: etichette, meseEvidenziato: meseScelto };

  disegnaGraficoColonne(document.getElementById("graficoIscritti"), {
    ...comune, etichettaUltimo: true,
    serie: [{ nome: scuola ? "Iscrizioni attive" : "Iscritti attivi", colore: COLORI.serie, valori: valori("iscrittiAttivi") }]
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

