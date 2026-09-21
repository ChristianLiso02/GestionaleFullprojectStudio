const GIORNI_LABEL_DETT = { MONDAY: "Lun", TUESDAY: "Mar", WEDNESDAY: "Mer", THURSDAY: "Gio", FRIDAY: "Ven", SATURDAY: "Sab", SUNDAY: "Dom" };
const corsoId = new URLSearchParams(window.location.search).get("id");

function renderCorsoInfo(c) {
  document.getElementById("corsoNome").textContent = c.nome;
  document.getElementById("linkModifica").href = `corso-form.html?id=${c.id}`;
  document.getElementById("linkNuovaIscrizione").href = `iscrizione-form.html?corsoId=${c.id}`;

  const giorni = (c.giorniSettimana || []).map(g => GIORNI_LABEL_DETT[g]).join(" · ");
  document.getElementById("corsoInfo").innerHTML = `
    <div class="row2">
      <div class="field"><label>Stile / Livello</label><div>${c.stile} &middot; ${c.livello}</div></div>
      <div class="field"><label>Istruttore</label><div>${escapeHtml(c.istruttoreNome) || "-"}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Sala</label><div>${escapeHtml(c.salaNome) || "-"}</div></div>
      <div class="field"><label>Orario</label><div>${giorni || "-"} ${c.orarioInizio || ""} - ${c.orarioFine || ""}</div></div>
    </div>
    <div class="row2">
      <div class="field"><label>Prezzo mensile</label><div>${c.prezzoMensile != null ? formatEuro(c.prezzoMensile) : "-"}</div></div>
      <div class="field"><label>Stato</label><div><span class="pill ${c.attivo ? "pill-success" : "pill-muted"}">${c.attivo ? "Attivo" : "Inattivo"}</span></div></div>
    </div>
  `;
}

function renderIscritti(iscrizioni) {
  const body = document.getElementById("iscrittiBody");
  body.innerHTML = iscrizioni.length === 0
    ? `<tr><td colspan="6" class="empty-state">Nessuno studente iscritto a questo corso.</td></tr>`
    : iscrizioni.map(i => `
        <tr>
          <td><a href="studente-dettaglio.html?id=${i.studenteId}"><b>${escapeHtml(i.studenteNomeCompleto)}</b></a></td>
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

async function init() {
  if (!corsoId) { showError("Corso non specificato."); return; }
  try {
    const [corso, iscrizioni] = await Promise.all([
      api.get(`/api/corsi/${corsoId}`),
      api.get(`/api/iscrizioni?corsoId=${corsoId}`)
    ]);
    renderCorsoInfo(corso);
    renderIscritti(iscrizioni);
  } catch (err) {
    showError(err.message);
  }
}

init();
