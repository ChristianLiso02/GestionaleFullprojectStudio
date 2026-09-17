let studentiCache = [];

async function loadStudenti(ricerca) {
  try {
    const query = ricerca ? `?ricerca=${encodeURIComponent(ricerca)}` : "";
    studentiCache = await api.get("/api/studenti" + query);
    renderStudenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderStudenti() {
  const body = document.getElementById("studentiBody");
  body.innerHTML = studentiCache.length === 0
    ? `<tr><td colspan="5" class="empty-state">Nessuno studente trovato.</td></tr>`
    : studentiCache.map(s => `
        <tr>
          <td><b>${escapeHtml(s.nome)} ${escapeHtml(s.cognome)}</b></td>
          <td>${escapeHtml(s.telefono)}<br><span style="color:var(--ink-faint);font-size:0.78rem">${escapeHtml(s.email)}</span></td>
          <td>${formatDate(s.dataIscrizione)}</td>
          <td><span class="pill ${s.attivo ? "pill-success" : "pill-muted"}">${s.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="studente-form.html?id=${s.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaStudente(${s.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaStudente(id) {
  if (!confirm("Eliminare questo studente? Se ha iscrizioni o pagamenti collegati, l'eliminazione verrà bloccata dal database.")) return;
  try {
    await api.del(`/api/studenti/${id}`);
    await loadStudenti(document.getElementById("ricercaInput").value.trim());
  } catch (err) { showError(err.message); }
}

let ricercaTimeout;
document.getElementById("ricercaInput").addEventListener("input", (e) => {
  clearTimeout(ricercaTimeout);
  ricercaTimeout = setTimeout(() => loadStudenti(e.target.value.trim()), 300);
});

loadStudenti();
