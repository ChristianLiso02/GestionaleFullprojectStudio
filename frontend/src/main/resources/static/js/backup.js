async function caricaBackup() {
  try {
    const archivio = await api.get("/api/backup");
    document.getElementById("spiegazione").innerHTML =
      `Ogni notte alle <b>${escapeHtml(archivio.orarioAutomatico)}</b> il gestionale salva da solo un <b>file Excel</b> ` +
      `con quote da incassare, anagrafiche, iscrizioni, pagamenti e presenze recenti: si apre con Excel anche se il gestionale ` +
      `non funzionasse. Dove previsto salva anche una <b>copia completa dei dati</b> (file .zip), che serve per ricostruire tutto.`;
    document.getElementById("cartella").textContent = "Cartella dei backup: " + archivio.cartella;

    const body = document.getElementById("backupBody");
    body.innerHTML = archivio.file.length === 0
      ? `<tr><td colspan="5" class="empty-state">Ancora nessun backup. Il primo verrà creato stanotte, oppure crealo adesso con il pulsante qui sopra.</td></tr>`
      : archivio.file.map(f => `
          <tr>
            <td>${f.tipo === "EXCEL"
              ? '<span class="pill pill-success">Excel</span>'
              : '<span class="pill pill-muted">Copia completa</span>'}</td>
            <td>${formatDataOra(f.data)}</td>
            <td class="testo-tenue">${escapeHtml(f.nome)}</td>
            <td>${formatDimensione(f.dimensione)}</td>
            <td class="cell-actions"><button type="button" class="btn btn-ghost btn-sm" data-nome="${escapeHtml(f.nome)}">${icona("backup")}Scarica</button></td>
          </tr>`).join("");
  } catch (err) {
    showError(err.message);
  }
}

function formatDataOra(iso) {
  if (!iso) return "-";
  const [data, ora] = iso.split("T");
  return `${formatDate(data)} alle ${(ora || "").slice(0, 5)}`;
}

document.getElementById("backupBody").addEventListener("click", async e => {
  const btn = e.target.closest("button[data-nome]");
  if (!btn) return;
  btn.disabled = true;
  try {
    await scaricaFile(`/api/backup/file/${encodeURIComponent(btn.dataset.nome)}`, btn.dataset.nome);
  } catch (err) {
    showError(err.message);
  } finally {
    btn.disabled = false;
  }
});

document.getElementById("btnGenera").addEventListener("click", async () => {
  const btn = document.getElementById("btnGenera");
  const box = document.getElementById("successBox");
  btn.disabled = true;
  btn.textContent = "Creazione in corso…";
  box.hidden = true;
  try {
    await api.post("/api/backup/genera");
    box.textContent = "Backup Excel creato: lo trovi in cima all'elenco.";
    box.hidden = false;
    await caricaBackup();
  } catch (err) {
    showError(err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = "Crea backup Excel adesso";
  }
});

caricaBackup();
