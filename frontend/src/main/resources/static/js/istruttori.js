let istruttoriCache = [];

async function loadIstruttori() {
  try {
    istruttoriCache = await api.get("/api/istruttori");
    renderIstruttori();
  } catch (err) {
    showError(err.message);
  }
}

function renderIstruttori() {
  const body = document.getElementById("istruttoriBody");
  body.innerHTML = istruttoriCache.length === 0
    ? `<tr><td colspan="6" class="empty-state">Nessun istruttore registrato.</td></tr>`
    : istruttoriCache.map(i => `
        <tr>
          <td><b>${escapeHtml(i.nome)} ${escapeHtml(i.cognome)}</b></td>
          <td>${escapeHtml(i.telefono)}</td>
          <td>${escapeHtml(i.email)}</td>
          <td>${(i.specializzazioni || []).join(", ")}</td>
          <td><span class="pill ${i.attivo ? "pill-success" : "pill-muted"}">${i.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="istruttore-form.html?id=${i.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaIstruttore(${i.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaIstruttore(id) {
  if (!confirm("Eliminare questo istruttore?")) return;
  try {
    await api.del(`/api/istruttori/${id}`);
    await loadIstruttori();
  } catch (err) { showError(err.message); }
}

loadIstruttori();
