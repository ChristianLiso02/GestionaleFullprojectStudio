let saleCache = [];

async function loadSale() {
  try {
    saleCache = await api.get("/api/sale");
    renderSale();
  } catch (err) {
    showError(err.message);
  }
}

function renderSale() {
  const body = document.getElementById("saleBody");
  body.innerHTML = saleCache.length === 0
    ? `<tr><td colspan="4" class="empty-state">Nessuna sala registrata.</td></tr>`
    : saleCache.map(s => `
        <tr>
          <td><b>${escapeHtml(s.nome)}</b></td>
          <td>${s.capienza ?? "-"}</td>
          <td>${escapeHtml(s.note)}</td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="sala-form.html?id=${s.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaSala(${s.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaSala(id) {
  if (!confirm("Eliminare questa sala?")) return;
  try {
    await api.del(`/api/sale/${id}`);
    await loadSale();
  } catch (err) { showError(err.message); }
}

loadSale();
