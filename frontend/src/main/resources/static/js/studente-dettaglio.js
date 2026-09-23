const studenteDettId = new URLSearchParams(window.location.search).get("id");

function renderStudenteInfo(s) {
  document.getElementById("studenteNome").textContent = `${s.nome} ${s.cognome}`;
  document.getElementById("linkModifica").href = `studente-form.html?id=${s.id}`;
  document.getElementById("linkNuovaIscrizione").href = `iscrizione-form.html?studenteId=${s.id}`;
  document.getElementById("linkNuovoPagamento").href = `pagamento-form.html?studenteId=${s.id}`;

  document.getElementById("studenteInfo").innerHTML = `
    <div class="row2">
      <div class="field"><label>Contatti</label><div>${escapeHtml(s.telefono) || "-"}<br>${escapeHtml(s.email) || "-"}</div></div>
      <div class="field"><label>Codice fiscale</label><div>${escapeHtml(s.codiceFiscale) || "-"}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Sesso</label><div>${s.sesso === "UOMO" ? "Uomo" : s.sesso === "DONNA" ? "Donna" : "Non specificato"}</div></div>
      <div class="field"><label>Iscritto dal</label><div>${formatDate(s.dataIscrizione)}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Stato</label><div><span class="pill ${s.attivo ? "pill-success" : "pill-muted"}">${s.attivo ? "Attivo" : "Inattivo"}</span></div></div>
    </div>
  `;
}

function renderIscrizioni(iscrizioni) {
  const body = document.getElementById("iscrizioniBody");
  body.innerHTML = iscrizioni.length === 0
    ? `<tr><td colspan="7" class="empty-state">Nessuna iscrizione a corsi.</td></tr>`
    : iscrizioni.map(i => `
        <tr>
          <td><a href="corso-dettaglio.html?id=${i.corsoId}"><b>${escapeHtml(i.corsoNome)}</b></a></td>
          <td>${escapeHtml(i.stagioneNome) || "-"}</td>
          <td>${escapeHtml(i.tipoAbbonamentoNome) || "-"}</td>
          <td>${formatDate(i.dataIscrizione)}</td>
          <td>${i.dataScadenza ? formatDate(i.dataScadenza) : "-"}</td>
          <td><span class="pill ${pillClass(i.stato)}">${i.stato}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="iscrizione-form.html?id=${i.id}">Modifica</a>
          </td>
        </tr>
      `).join("");
}

function renderPagamenti(pagamenti) {
  const body = document.getElementById("pagamentiBody");
  body.innerHTML = pagamenti.length === 0
    ? `<tr><td colspan="6" class="empty-state">Nessun pagamento registrato.</td></tr>`
    : pagamenti.map(p => `
        <tr>
          <td>${formatDate(p.dataPagamento)}</td>
          <td>${formatEuro(p.importo)}</td>
          <td>${p.metodo}</td>
          <td>${escapeHtml(p.causale) || "-"}</td>
          <td><span class="pill ${pillClass(p.stato)}">${p.stato}</span></td>
          <td class="cell-actions">
            <a class="btn btn-ghost btn-sm" href="pagamento-form.html?id=${p.id}">Modifica</a>
          </td>
        </tr>
      `).join("");
}

function renderPresenze(presenze) {
  const body = document.getElementById("presenzeBody");
  body.innerHTML = presenze.length === 0
    ? `<tr><td colspan="3" class="empty-state">Nessuna presenza registrata.</td></tr>`
    : presenze.map(p => `
        <tr>
          <td>${formatDate(p.dataLezione)}</td>
          <td>${escapeHtml(p.corsoNome)}</td>
          <td><span class="pill ${p.presente ? "pill-success" : "pill-danger"}">${p.presente ? "Presente" : "Assente"}</span></td>
        </tr>
      `).join("");
}

async function init() {
  if (!studenteDettId) { showError("Studente non specificato."); return; }
  try {
    const [studente, iscrizioni, pagamenti, presenze] = await Promise.all([
      api.get(`/api/studenti/${studenteDettId}`),
      api.get(`/api/iscrizioni?studenteId=${studenteDettId}`),
      api.get(`/api/pagamenti?studenteId=${studenteDettId}`),
      api.get(`/api/presenze?studenteId=${studenteDettId}`)
    ]);
    renderStudenteInfo(studente);
    renderIscrizioni(iscrizioni);
    renderPagamenti(pagamenti);
    renderPresenze(presenze);
  } catch (err) {
    showError(err.message);
  }
}

init();
