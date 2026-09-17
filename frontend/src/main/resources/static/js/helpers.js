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

function openModal(title, bodyHtml, onSave) {
  document.getElementById("modalTitle").textContent = title;
  document.getElementById("modalBody").innerHTML = bodyHtml;
  document.getElementById("modalBackdrop").hidden = false;
  const saveBtn = document.getElementById("modalSave");
  saveBtn.onclick = async () => {
    const ok = await onSave();
    if (ok !== false) closeModal();
  };
}

function closeModal() {
  document.getElementById("modalBackdrop").hidden = true;
}

function showError(message) {
  const box = document.getElementById("errorBox");
  if (!box) { alert(message); return; }
  box.textContent = message;
  box.hidden = false;
  setTimeout(() => { box.hidden = true; }, 5000);
}

function pillClass(stato) {
  const map = {
    "ATTIVA": "pill-success", "ATTIVO": "pill-success", "PAGATO": "pill-success",
    "SCADUTA": "pill-warning", "IN_SOSPESO": "pill-warning",
    "ANNULLATA": "pill-danger", "RIMBORSATO": "pill-danger", "INATTIVO": "pill-muted"
  };
  return map[stato] || "pill-muted";
}
