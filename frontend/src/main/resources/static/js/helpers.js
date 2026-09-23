function escapeHtml(value) {
  if (value === null || value === undefined) return "";
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatDate(iso) {
  if (!iso) return "-";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
}

function formatEuro(n) {
  if (n === null || n === undefined) return "-";
  return "€ " + Number(n).toLocaleString("it-IT", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

function showError(message) {
  const box = document.getElementById("errorBox");
  if (!box) { alert(message); return; }
  box.textContent = message;
  box.hidden = false;
  setTimeout(() => { box.hidden = true; }, 5000);
}

function setFieldError(inputId, message) {
  const input = document.getElementById(inputId);
  if (!input) return;
  input.classList.add("field-invalid");
  let msg = document.getElementById(inputId + "-error");
  if (!msg) {
    msg = document.createElement("div");
    msg.id = inputId + "-error";
    msg.className = "field-error-msg";
    input.insertAdjacentElement("afterend", msg);
  }
  msg.textContent = message;
}

function clearFieldError(inputId) {
  const input = document.getElementById(inputId);
  if (input) input.classList.remove("field-invalid");
  const msg = document.getElementById(inputId + "-error");
  if (msg) msg.remove();
}

function clearAllFieldErrors(form) {
  form.querySelectorAll(".field-invalid").forEach(el => el.classList.remove("field-invalid"));
  form.querySelectorAll(".field-error-msg").forEach(el => el.remove());
}

async function initSelettoreStagione(selectId, onChange) {
  const select = document.getElementById(selectId);
  try {
    const stagioni = await api.get("/api/stagioni");
    select.innerHTML = stagioni.map(s =>
      `<option value="${s.id}">${escapeHtml(s.nome)}${s.corrente ? " (corrente)" : ""}</option>`
    ).join("");
    const corrente = stagioni.find(s => s.corrente);
    if (corrente) select.value = corrente.id;
    select.addEventListener("change", () => onChange(select.value ? parseInt(select.value) : null));
    onChange(select.value ? parseInt(select.value) : null);
  } catch (err) {
    showError(err.message);
  }
}

function formatPosti(iscrittiAttivi, capienzaMax) {
  if (capienzaMax == null) return String(iscrittiAttivi ?? 0);
  const pieno = (iscrittiAttivi ?? 0) >= capienzaMax;
  return pieno
    ? `<span class="pill pill-danger">${iscrittiAttivi}/${capienzaMax} pieno</span>`
    : `${iscrittiAttivi}/${capienzaMax}`;
}

function formatUominiDonne(uomini, donne, totale) {
  const u = uomini ?? 0;
  const d = donne ?? 0;
  const nd = Math.max((totale ?? 0) - u - d, 0);
  const ndHtml = nd > 0 ? `<div class="bilancia-nd">+${nd} non specificati</div>` : "";
  if (u + d === 0) return nd > 0 ? ndHtml : "-";
  const pu = Math.round((u * 100) / (u + d));
  return `
    <div class="bilancia" title="${u} uomini (${pu}%), ${d} donne (${100 - pu}%)">
      <div class="bilancia-testo"><span class="u">U ${u}</span><span class="d">D ${d}</span></div>
      <div class="bilancia-barra"><span class="u" style="width:${pu}%"></span><span class="d" style="width:${100 - pu}%"></span></div>
      ${ndHtml}
    </div>`;
}

const NOMI_MESI = ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
  "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"];

// "2026-10" -> "Ottobre 2026"
function formatMese(annoMese) {
  if (!annoMese) return "-";
  const [anno, mese] = annoMese.split("-");
  return `${NOMI_MESI[parseInt(mese, 10) - 1]} ${anno}`;
}

function aggiungiMesi(annoMese, n) {
  const [anno, mese] = annoMese.split("-").map(Number);
  const totale = anno * 12 + (mese - 1) + n;
  return `${Math.floor(totale / 12)}-${String((totale % 12) + 1).padStart(2, "0")}`;
}

function formatPeriodo(annoMese, mesi) {
  if (!annoMese) return "-";
  return mesi > 1 ? `${formatMese(annoMese)} – ${formatMese(aggiungiMesi(annoMese, mesi - 1))}` : formatMese(annoMese);
}

function pillClass(stato) {
  const map = {
    "ATTIVA": "pill-success", "ATTIVO": "pill-success", "PAGATO": "pill-success",
    "RITIRATO": "pill-muted", "INATTIVO": "pill-muted"
  };
  return map[stato] || "pill-muted";
}

function statoIscrizioneHtml(i) {
  const ritiro = i.stato === "RITIRATO" && i.dataRitiro
    ? `<br><span class="testo-tenue">dal ${formatDate(i.dataRitiro)}</span>` : "";
  return `<span class="pill ${pillClass(i.stato)}">${i.stato}</span>${ritiro}`;
}

function azioneStatoIscrizione(i) {
  return i.stato === "RITIRATO"
    ? `<button class="btn btn-ghost btn-sm" onclick="riattivaIscrizione(${i.id})">Riattiva</button>`
    : `<button class="btn btn-ghost btn-sm" onclick="ritiraIscrizione(${i.id})">Ritira</button>`;
}

// Finestra con un campo data (default oggi, niente date future). Restituisce la data scelta o null se annullato.
function chiediData({ titolo, testo, conferma, valore }) {
  return new Promise(resolve => {
    const oggi = todayISO();
    const dialogo = document.createElement("dialog");
    dialogo.className = "dialogo";
    dialogo.innerHTML = `
      <form method="dialog">
        <h3>${titolo}</h3>
        <p>${testo}</p>
        <div class="field"><label>Data</label><input type="date" name="data" value="${valore || oggi}" max="${oggi}" required></div>
        <div class="dialogo-azioni">
          <button value="annulla" class="btn btn-ghost" formnovalidate>Annulla</button>
          <button value="ok" class="btn btn-accent">${conferma}</button>
        </div>
      </form>`;
    document.body.appendChild(dialogo);
    dialogo.addEventListener("close", () => {
      const data = dialogo.returnValue === "ok" ? dialogo.querySelector("input").value : null;
      dialogo.remove();
      resolve(data);
    });
    dialogo.showModal();
  });
}

// dataProposta: per chi ha smesso di venire senza avvisare, l'inizio del primo mese non pagato.
async function ritiraIscrizione(id, dataProposta) {
  const testo = dataProposta
    ? "Lo studente non ha rinnovato: se ha smesso di venire senza avvisare, segnalo come ritirato. " +
      "La data proposta è l'inizio del primo mese non pagato, così le quote scadute non restano come debito. Puoi cambiarla."
    : "Da quale giorno lo studente non frequenta più il corso? Se è entro la prima settimana del mese, la quota di quel mese non sarà dovuta. " +
      "Puoi indicare anche una data passata (ad esempio l'inizio di una malattia) e riattivarlo quando torna: i mesi da ritirato non saranno dovuti.";
  const data = await chiediData({ titolo: "Segna come ritirato", testo, conferma: "Segna ritirato", valore: dataProposta });
  if (!data) return;
  try {
    await api.put(`/api/iscrizioni/${id}/ritira?data=${data}`);
    window.location.reload();
  } catch (err) { showError(err.message); }
}

async function riattivaIscrizione(id) {
  const data = await chiediData({
    titolo: "Riattiva iscrizione",
    testo: "Da quale giorno lo studente torna a frequentare? Ricomincerà a pagare dal mese del rientro; i mesi in cui era ritirato non sono dovuti.",
    conferma: "Riattiva"
  });
  if (!data) return;
  try {
    await api.put(`/api/iscrizioni/${id}/riattiva?data=${data}`);
    window.location.reload();
  } catch (err) { showError(err.message); }
}

// I pagamenti hanno un solo stato (effettuato). Quelli salvati in passato come "in sospeso" o
// "rimborsato" non contano come incasso: lo si segnala sotto l'importo.
function notaVecchioStatoPagamento(p) {
  if (!p.stato || p.stato === "PAGATO") return "";
  const nome = p.stato === "IN_SOSPESO" ? "in sospeso" : "rimborsato";
  return `<br><span class="testo-tenue">${nome} · non conta</span>`;
}
