// Grafici a colonne in SVG, senza librerie esterne (il gestionale deve funzionare anche offline).
// Supporta: una serie, serie impilate e serie divergenti (sopra/sotto lo zero). Ogni colonna ha un
// tooltip al passaggio del mouse o del focus da tastiera; i valori restano comunque nella tabella.

const SVG_NS = "http://www.w3.org/2000/svg";
const GRAFICO_ALTEZZA = 230;
const MARGINE = { alto: 22, destra: 12, basso: 36, sinistra: 56 };
const INK = { testo: "#141110", tenue: "#8a8078", griglia: "#ece7e3", asse: "#cfc7c1", superficie: "#ffffff", velo: "#f5f2ef" };
const MESI_BREVI = ["Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"];

function elementoSvg(nome, attributi = {}) {
  const el = document.createElementNS(SVG_NS, nome);
  Object.entries(attributi).forEach(([k, v]) => el.setAttribute(k, v));
  return el;
}

// Passo "tondo" per le tacche dell'asse (1, 2, 2.5, 5 × 10^n), con al massimo ~5 tacche.
function scalaTonda(massimo, intero) {
  if (massimo <= 0) massimo = intero ? 4 : 100;
  const grezzo = massimo / 4;
  const magnitudine = Math.pow(10, Math.floor(Math.log10(grezzo)));
  const normalizzato = grezzo / magnitudine;
  let passo = (normalizzato <= 1 ? 1 : normalizzato <= 2 ? 2 : normalizzato <= 2.5 ? 2.5 : normalizzato <= 5 ? 5 : 10) * magnitudine;
  if (intero) passo = Math.max(1, Math.ceil(passo));
  return { massimo: Math.ceil(massimo / passo) * passo, passo };
}

// Colonna con estremità dati arrotondata (4px) e base squadrata sulla linea di zero.
function percorsoColonna(x, larghezza, yBase, yCima) {
  const altezza = Math.abs(yBase - yCima);
  if (altezza < 0.5) return "";
  const r = Math.min(4, larghezza / 2, altezza);
  const su = yCima < yBase;
  const s = su ? 1 : -1;
  return [
    `M${x},${yBase}`,
    `V${yCima + s * r}`,
    `Q${x},${yCima} ${x + r},${yCima}`,
    `H${x + larghezza - r}`,
    `Q${x + larghezza},${yCima} ${x + larghezza},${yCima + s * r}`,
    `V${yBase}`,
    "Z"
  ].join(" ");
}

function etichettaMese(annoMese, indice) {
  const [anno, mese] = annoMese.split("-");
  const nome = MESI_BREVI[parseInt(mese, 10) - 1];
  return { nome, anno: indice === 0 || mese === "01" ? anno : "" };
}

/**
 * opzioni:
 *   mesi: ["2026-09", ...]
 *   serie: [{ nome, colore, valori: [..], verso: 1 | -1 }]  (verso -1 = sotto lo zero)
 *   impilato: true per sommare le serie nella stessa colonna
 *   formato: funzione valore -> testo (es. formatEuro)
 *   intero: true se i valori sono conteggi
 *   etichettaUltimo: true per scrivere il valore sull'ultima colonna (solo serie singola)
 *   meseEvidenziato: "2026-10" per mettere in grassetto l'etichetta del mese scelto
 */
function disegnaGraficoColonne(contenitore, opzioni) {
  contenitore._opzioniGrafico = opzioni;
  contenitore.innerHTML = "";
  contenitore.classList.add("grafico");

  const { mesi, serie, impilato = false, formato = v => String(v), intero = true, etichettaUltimo = false, meseEvidenziato } = opzioni;
  const larghezza = Math.max(contenitore.clientWidth, 280);
  const altezza = GRAFICO_ALTEZZA;
  const areaL = larghezza - MARGINE.sinistra - MARGINE.destra;
  const areaA = altezza - MARGINE.alto - MARGINE.basso;

  if (serie.length > 1) {
    const legenda = document.createElement("div");
    legenda.className = "grafico-legenda";
    serie.forEach(s => {
      const voce = document.createElement("span");
      const campione = document.createElement("i");
      campione.style.background = s.colore;
      voce.appendChild(campione);
      voce.appendChild(document.createTextNode(s.nome));
      legenda.appendChild(voce);
    });
    contenitore.appendChild(legenda);
  }

  const sopra = serie.filter(s => (s.verso || 1) === 1);
  const sotto = serie.filter(s => s.verso === -1);
  const totale = (gruppo, i) => impilato ? gruppo.reduce((t, s) => t + s.valori[i], 0) : Math.max(0, ...gruppo.map(s => s.valori[i]));
  const maxSopra = Math.max(0, ...mesi.map((_, i) => totale(sopra, i)));
  const maxSotto = sotto.length ? Math.max(0, ...mesi.map((_, i) => totale(sotto, i))) : 0;
  const scala = scalaTonda(Math.max(maxSopra, maxSotto), intero);
  const minimo = sotto.length ? -scala.massimo : 0;
  const y = v => MARGINE.alto + (scala.massimo - v) / (scala.massimo - minimo) * areaA;

  const svg = elementoSvg("svg", { width: larghezza, height: altezza, viewBox: `0 0 ${larghezza} ${altezza}`, role: "img" });
  svg.setAttribute("aria-label", serie.map(s => s.nome).join(", "));

  for (let v = minimo; v <= scala.massimo + 1e-9; v += scala.passo) {
    const yy = Math.round(y(v)) + 0.5;
    svg.appendChild(elementoSvg("line", { x1: MARGINE.sinistra, x2: larghezza - MARGINE.destra, y1: yy, y2: yy, stroke: v === 0 ? INK.asse : INK.griglia, "stroke-width": 1 }));
    const tacca = elementoSvg("text", { x: MARGINE.sinistra - 8, y: yy + 4, "text-anchor": "end", class: "grafico-tacca" });
    tacca.textContent = formato(Math.abs(v));
    svg.appendChild(tacca);
  }

  const banda = areaL / mesi.length;
  const gruppiAffiancati = impilato ? 1 : Math.max(sopra.length, sotto.length, 1);
  const larghezzaColonna = Math.min(24, (banda * 0.6) / gruppiAffiancati);
  const GAP = 2;

  mesi.forEach((mese, i) => {
    const x0 = MARGINE.sinistra + i * banda;
    const gruppo = elementoSvg("g");

    const velo = elementoSvg("rect", { x: x0 + 1, y: MARGINE.alto, width: banda - 2, height: areaA, fill: "transparent", rx: 4, class: "grafico-velo" });
    gruppo.appendChild(velo);

    [[sopra, 1], [sotto, -1]].forEach(([gruppoSerie, verso]) => {
      const inizioX = x0 + (banda - larghezzaColonna * (impilato ? 1 : gruppoSerie.length) - GAP * (impilato ? 0 : gruppoSerie.length - 1)) / 2;
      let accumulato = 0;
      gruppoSerie.forEach((s, k) => {
        const valore = s.valori[i];
        if (!valore) return;
        const x = impilato ? inizioX : inizioX + k * (larghezzaColonna + GAP);
        const base = impilato ? accumulato : 0;
        const yBase = y(verso * base) - (impilato && base > 0 ? verso * GAP : 0);
        const yCima = y(verso * (base + valore));
        accumulato += valore;
        const d = percorsoColonna(x, larghezzaColonna, yBase, yCima);
        if (d) gruppo.appendChild(elementoSvg("path", { d, fill: s.colore }));
      });
    });

    if (etichettaUltimo && serie.length === 1 && i === mesi.length - 1 && serie[0].valori[i]) {
      const valore = elementoSvg("text", { x: x0 + banda / 2, y: y(serie[0].valori[i]) - 6, "text-anchor": "middle", class: "grafico-valore" });
      valore.textContent = formato(serie[0].valori[i]);
      gruppo.appendChild(valore);
    }

    const { nome, anno } = etichettaMese(mese, i);
    const etichetta = elementoSvg("text", { x: x0 + banda / 2, y: altezza - MARGINE.basso + 16, "text-anchor": "middle", class: "grafico-mese" + (mese === meseEvidenziato ? " scelto" : "") });
    etichetta.textContent = nome;
    gruppo.appendChild(etichetta);
    if (anno) {
      const etichettaAnno = elementoSvg("text", { x: x0 + banda / 2, y: altezza - MARGINE.basso + 29, "text-anchor": "middle", class: "grafico-tacca" });
      etichettaAnno.textContent = anno;
      gruppo.appendChild(etichettaAnno);
    }

    // L'area sensibile è tutta la banda del mese, più grande della colonna.
    const area = elementoSvg("rect", { x: x0, y: MARGINE.alto, width: banda, height: areaA, fill: "transparent", tabindex: 0, class: "grafico-area" });
    const mostra = () => { velo.setAttribute("fill", INK.velo); mostraTooltip(contenitore, mese, serie, i, formato, x0, banda, larghezza); };
    const nascondi = () => { velo.setAttribute("fill", "transparent"); nascondiTooltip(contenitore); };
    area.addEventListener("pointerenter", mostra);
    area.addEventListener("pointerleave", nascondi);
    area.addEventListener("focus", mostra);
    area.addEventListener("blur", nascondi);
    gruppo.appendChild(area);

    svg.appendChild(gruppo);
  });

  contenitore.appendChild(svg);
}

// Il tooltip si affianca alla colonna (a sinistra se è nella metà destra), così non la copre.
function mostraTooltip(contenitore, mese, serie, indice, formato, xBanda, banda, larghezza) {
  let tooltip = contenitore.querySelector(".grafico-tooltip");
  if (!tooltip) {
    tooltip = document.createElement("div");
    tooltip.className = "grafico-tooltip";
    contenitore.appendChild(tooltip);
  }
  tooltip.replaceChildren();
  const titolo = document.createElement("div");
  titolo.className = "grafico-tooltip-titolo";
  titolo.textContent = formatMese(mese);
  tooltip.appendChild(titolo);
  serie.forEach(s => {
    const riga = document.createElement("div");
    riga.className = "grafico-tooltip-riga";
    const chiave = document.createElement("i");
    chiave.style.background = s.colore;
    const valore = document.createElement("b");
    valore.textContent = formato(s.valori[indice]);
    const nome = document.createElement("span");
    nome.textContent = s.nome;
    riga.append(chiave, valore, nome);
    tooltip.appendChild(riga);
  });
  tooltip.hidden = false;
  const offsetLegenda = contenitore.querySelector(".grafico-legenda")?.offsetHeight || 0;
  const w = tooltip.offsetWidth;
  const aSinistra = xBanda + banda / 2 > larghezza / 2;
  const sinistra = aSinistra ? xBanda - w - 6 : xBanda + banda + 6;
  tooltip.style.left = `${Math.min(Math.max(sinistra, 0), larghezza - w)}px`;
  tooltip.style.top = `${offsetLegenda + MARGINE.alto}px`;
}

function nascondiTooltip(contenitore) {
  const tooltip = contenitore.querySelector(".grafico-tooltip");
  if (tooltip) tooltip.hidden = true;
}

let ridisegnoTimer;
window.addEventListener("resize", () => {
  clearTimeout(ridisegnoTimer);
  ridisegnoTimer = setTimeout(() => {
    document.querySelectorAll(".grafico").forEach(el => {
      if (el._opzioniGrafico) disegnaGraficoColonne(el, el._opzioniGrafico);
    });
  }, 150);
});
