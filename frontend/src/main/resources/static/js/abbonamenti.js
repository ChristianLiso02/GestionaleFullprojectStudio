let abbonamentiCache = [];

async function loadAbbonamenti() {
  try {
    abbonamentiCache = await api.get("/api/abbonamenti");
    renderAbbonamenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderAbbonamenti() {
  const body = document.getElementById("abbonamentiBody");
  body.innerHTML = abbonamentiCache.length === 0
    ? `<tr><td colspan="7" class="empty-state">Nessun tipo di abbonamento registrato.</td></tr>`
    : abbonamentiCache.map(a => `
        <tr>
          <td><b>${escapeHtml(a.nome)}</b></td>
          <td>${escapeHtml(a.descrizione)}</td>
          <td>${a.durataGiorni ?? "-"}</td>
          <td>${a.numeroLezioni ?? "-"}</td>
          <td>${formatEuro(a.prezzo)}</td>
          <td><span class="pill ${a.attivo ? "pill-success" : "pill-muted"}">${a.attivo ? "Attivo" : "Inattivo"}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="abbonamento-form.html?id=${a.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaAbbonamento(${a.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaAbbonamento(id) {
  if (!confirm("Eliminare questo tipo di abbonamento?")) return;
  try {
    await api.del(`/api/abbonamenti/${id}`);
    await loadAbbonamenti();
  } catch (err) { showError(err.message); }
}

loadAbbonamenti();
