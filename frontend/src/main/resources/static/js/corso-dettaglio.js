const GIORNI_LABEL_DETT = { MONDAY: "Lun", TUESDAY: "Mar", WEDNESDAY: "Mer", THURSDAY: "Gio", FRIDAY: "Ven", SATURDAY: "Sab", SUNDAY: "Dom" };
const corsoId = new URLSearchParams(window.location.search).get("id");
let iscrittiAttiviCache = [];

function renderCorsoInfo(c) {
  document.getElementById("corsoNome").textContent = c.nome;
  document.getElementById("linkModifica").href = `corso-form.html?id=${c.id}`;
  document.getElementById("linkNuovaIscrizione").href = `iscrizione-form.html?corsoId=${c.id}`;

  const giorni = (c.giorniSettimana || []).map(g => GIORNI_LABEL_DETT[g]).join(" · ");
  document.getElementById("corsoInfo").innerHTML = `
    <div class="row2">
      <div class="field"><label>Stile / Livello</label><div>${c.stile} &middot; ${c.livello}</div></div>
      <div class="field"><label>Istruttori</label><div>${escapeHtml((c.istruttoriNomi || []).join(" + ")) || "-"}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Sala</label><div>${escapeHtml(c.salaNome) || "-"}</div></div>
      <div class="field"><label>Orario</label><div>${giorni || "-"} ${c.orarioInizio || ""} - ${c.orarioFine || ""}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Stagione</label><div>${escapeHtml(c.stagioneNome) || "-"}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Prezzo mensile</label><div>${c.prezzoMensile != null ? formatEuro(c.prezzoMensile) : "-"}</div></div>
      <div class="field"><label>Stato</label><div><span class="pill ${c.attivo ? "pill-success" : "pill-muted"}">${c.attivo ? "Attivo" : "Inattivo"}</span></div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Posti occupati</label><div>${formatPosti(c.iscrittiAttivi, c.capienzaMax)}</div></div>
      <div class="field"><label>Uomini / Donne (iscritti attivi)</label><div style="max-width:260px">${formatUominiDonne(c.iscrittiUomini, c.iscrittiDonne, c.iscrittiAttivi)}</div></div>
    </div>
  `;
}

function renderIscritti(iscrizioni) {
  const body = document.getElementById("iscrittiBody");
  body.innerHTML = iscrizioni.length === 0
    ? `<tr><td colspan="5" class="empty-state">Nessuno studente iscritto a questo corso.</td></tr>`
    : iscrizioni.map(i => `
        <tr>
          <td><a href="studente-dettaglio.html?id=${i.studenteId}"><b>${escapeHtml(i.studenteNomeCompleto)}</b></a></td>
          <td>${escapeHtml(i.tipoAbbonamentoNome) || "-"}</td>
          <td>${formatDate(i.dataIscrizione)}</td>
          <td>${statoIscrizioneHtml(i)}</td>
          <td class="cell-actions">
            ${azioneStatoIscrizione(i)}
            <a class="btn btn-ghost btn-sm" href="iscrizione-form.html?id=${i.id}">Modifica</a>
          </td>
        </tr>
      `).join("");
}

async function loadPresenzeDelGiorno() {
  const data = document.getElementById("dataPresenzeInput").value;
  const body = document.getElementById("presenzeCorsoBody");
  if (!data) { body.innerHTML = `<tr><td colspan="2" class="empty-state">Seleziona una data.</td></tr>`; return; }

  try {
    const presenze = await api.get(`/api/presenze?corsoId=${corsoId}&data=${data}`);
    const presenzeMap = {};
    presenze.forEach(p => { presenzeMap[p.iscrizioneId] = p.presente; });

    body.innerHTML = iscrittiAttiviCache.length === 0
      ? `<tr><td colspan="2" class="empty-state">Nessuno studente iscritto a questo corso.</td></tr>`
      : iscrittiAttiviCache.map(i => {
          const stato = presenzeMap[i.id];
          const label = stato === undefined ? "Non registrato" : (stato ? "Presente" : "Assente");
          const cls = stato === undefined ? "pill-muted" : (stato ? "pill-success" : "pill-danger");
          return `
            <tr>
              <td>${escapeHtml(i.studenteNomeCompleto)}</td>
              <td><span class="pill ${cls}">${label}</span></td>
            </tr>
          `;
        }).join("");
  } catch (err) {
    showError(err.message);
  }
}

// Statistiche del corso nella sua stagione, fino al mese corrente (o all'ultimo mese della stagione).
async function loadAndamento(corso) {
  if (!corso.stagioneId) return;
  try {
    const statistiche = await api.get(`/api/statistiche?stagioneId=${corso.stagioneId}`);
    const datiCorso = statistiche.corsi.find(c => c.corsoId === corso.id);
    if (!datiCorso || datiCorso.mesi.length === 0) return;
    const mesi = datiCorso.mesi;
    const ultimoMese = mesi[mesi.length - 1].mese;

    document.getElementById("sezioneAndamento").hidden = false;
    document.getElementById("titoloAndamento").textContent = `Andamento del corso · Stagione ${statistiche.stagioneNome}`;
    document.getElementById("linkStatistiche").href = `statistiche.html?stagione=${corso.stagioneId}&corso=${corso.id}`;
    renderKpiStatistiche(mesi, ultimoMese, false);
    renderGraficiStatistiche(mesi, ultimoMese, false);
  } catch (err) {
    showError(err.message);
  }
}

async function scaricaCorso() {
  const pulsante = document.getElementById("btnScarica");
  pulsante.disabled = true;
  pulsante.textContent = "Preparazione file...";
  try {
    await scaricaFile(`/api/corsi/${corsoId}/esporta`, `corso-${corsoId}.xlsx`);
  } catch (err) {
    showError(err.message);
  } finally {
    pulsante.disabled = false;
    pulsante.textContent = "Scarica Excel";
  }
}

document.getElementById("btnScarica").addEventListener("click", scaricaCorso);

async function init() {
  if (!corsoId) { showError("Corso non specificato."); return; }
  try {
    const [corso, iscrizioni] = await Promise.all([
      api.get(`/api/corsi/${corsoId}`),
      api.get(`/api/iscrizioni?corsoId=${corsoId}`)
    ]);
    renderCorsoInfo(corso);
    renderIscritti(iscrizioni);
    loadAndamento(corso);
    iscrittiAttiviCache = iscrizioni.filter(i => i.stato === "ATTIVA");

    document.getElementById("linkGestisciPresenze").href = `presenze.html?corsoId=${corsoId}`;
    document.getElementById("dataPresenzeInput").value = todayISO();
    document.getElementById("dataPresenzeInput").addEventListener("change", loadPresenzeDelGiorno);
    await loadPresenzeDelGiorno();
  } catch (err) {
    showError(err.message);
  }
}

init();
