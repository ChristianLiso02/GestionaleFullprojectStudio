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

function pillClass(stato) {
  const map = {
    "ATTIVA": "pill-success", "ATTIVO": "pill-success", "PAGATO": "pill-success",
    "SCADUTA": "pill-warning", "IN_SOSPESO": "pill-warning",
    "ANNULLATA": "pill-danger", "RIMBORSATO": "pill-danger", "INATTIVO": "pill-muted"
  };
  return map[stato] || "pill-muted";
}
