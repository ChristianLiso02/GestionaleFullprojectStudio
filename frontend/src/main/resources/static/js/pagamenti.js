let pagamentiCache = [];

async function loadPagamenti() {
  try {
    pagamentiCache = await api.get("/api/pagamenti");
    renderPagamenti();
  } catch (err) {
    showError(err.message);
  }
}

function renderPagamenti() {
  const body = document.getElementById("pagamentiBody");
  const rows = [...pagamentiCache].sort((a, b) => (a.dataPagamento < b.dataPagamento ? 1 : -1));
  body.innerHTML = rows.length === 0
    ? `<tr><td colspan="8" class="empty-state">Nessun pagamento registrato.</td></tr>`
    : rows.map(p => `
        <tr>
          <td><b>${escapeHtml(p.studenteNomeCompleto)}</b></td>
          <td>${formatDate(p.dataPagamento)}</td>
          <td>${formatPeriodo(p.meseRiferimento, p.mesiCoperti)}</td>
          <td>${formatEuro(p.importo)}</td>
          <td>${p.metodo}</td>
          <td>${escapeHtml(p.causale)}</td>
          <td><span class="pill ${pillClass(p.stato)}">${p.stato}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="pagamento-form.html?id=${p.id}">Modifica</a>
            <button class="btn btn-ghost btn-sm" onclick="eliminaPagamento(${p.id})">Elimina</button>
          </td>
        </tr>
      `).join("");
}

async function eliminaPagamento(id) {
  if (!confirm("Eliminare questo pagamento?")) return;
  try {
    await api.del(`/api/pagamenti/${id}`);
    await loadPagamenti();
  } catch (err) { showError(err.message); }
}

loadPagamenti();
