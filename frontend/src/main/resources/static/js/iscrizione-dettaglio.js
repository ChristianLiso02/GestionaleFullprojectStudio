// Pagina di un'iscrizione: la relazione tra uno studente e un corso, con le schede di entrambi,
// i dettagli dell'iscrizione, le quote scadute, i pagamenti e le presenze.
const iscrizioneId = new URLSearchParams(window.location.search).get("id");

function riga(etichetta, valore) {
  return `<dt>${etichetta}</dt><dd>${valore || '<span class="testo-tenue">-</span>'}</dd>`;
}

function renderRelazione(i) {
  document.getElementById("relazione").innerHTML = `
    <a class="entita" href="studente-dettaglio.html?id=${i.studenteId}">
      <span class="entita-icona studente">${icona("studenti")}</span>
      <span><span class="entita-tipo">Studente</span><br><span class="entita-nome">${escapeHtml(i.studenteNomeCompleto)}</span></span>
    </a>
    <span class="collegamento" aria-hidden="true">⟷</span>
    <a class="entita" href="corso-dettaglio.html?id=${i.corsoId}">
      <span class="entita-icona corso">${icona("corsi")}</span>
      <span><span class="entita-tipo">Corso</span><br><span class="entita-nome">${escapeHtml(i.corsoNome)}</span></span>
    </a>
    <span>${statoIscrizioneHtml(i)}</span>`;

  document.getElementById("azioniIscrizione").innerHTML = `
    ${azioneStatoIscrizione(i)}
    <a class="btn btn-ghost btn-sm" href="iscrizione-form.html?id=${i.id}">Modifica</a>
    <button type="button" class="btn btn-elimina btn-sm" onclick="eliminaIscrizione()">Elimina</button>`;
  document.getElementById("linkStudente").href = `studente-dettaglio.html?id=${i.studenteId}`;
  document.getElementById("linkCorso").href = `corso-dettaglio.html?id=${i.corsoId}`;
  document.getElementById("linkNuovoPagamento").href =
    `pagamento-form.html?studenteId=${i.studenteId}&iscrizioneId=${i.id}&ritorno=studente`;
}

function renderStudente(s) {
  const note = s.noteMediche ? `<span class="avviso-medico">${escapeHtml(s.noteMediche)}</span>` : "";
  document.getElementById("schedaStudente").innerHTML = `<dl>
    ${riga("Nome", `<b>${escapeHtml(s.nome)} ${escapeHtml(s.cognome)}</b>`)}
    ${riga("Sesso", s.sesso === "UOMO" ? "Uomo" : s.sesso === "DONNA" ? "Donna" : "")}
    ${riga("Telefono", s.telefono ? `<a href="tel:${escapeHtml(s.telefono)}">${escapeHtml(s.telefono)}</a>` : "")}
    ${riga("Email", s.email ? `<a href="mailto:${escapeHtml(s.email)}">${escapeHtml(s.email)}</a>` : "")}
    ${riga("Codice fiscale", escapeHtml(s.codiceFiscale))}
    ${riga("Data di nascita", s.dataNascita ? formatDate(s.dataNascita) : "")}
    ${riga("Contatto emergenza", escapeHtml(s.contattoEmergenza))}
    ${riga("Note mediche", note)}
  </dl>`;
}

function renderCorso(c) {
  document.getElementById("schedaCorso").innerHTML = `<dl>
    ${riga("Corso", `<b>${escapeHtml(c.nome)}</b>`)}
    ${riga("Stile e livello", `${formatEnum(c.stile)} · ${formatEnum(c.livello)}`)}
    ${riga("Giorni e orario", [formatGiorni(c.giorniSettimana), formatFasciaOraria(c)].filter(Boolean).join(", "))}
    ${riga("Sala", escapeHtml(c.salaNome))}
    ${riga("Istruttori", escapeHtml((c.istruttoriNomi || []).join(" + ")))}
    ${riga("Posti occupati", formatPosti(c.iscrittiAttivi, c.capienzaMax))}
    ${riga("Stagione", escapeHtml(c.stagioneNome))}
  </dl>`;
}

function renderIscrizione(i) {
  const quota = i.quotaImporto != null
    ? `${formatEuro(i.quotaImporto)} ${i.quotaMesi > 1 ? `ogni ${i.quotaMesi} mesi` : "al mese"}` : "";
  const ritiri = (i.ritiri || []).map(p =>
    `dal ${formatDate(p.dataRitiro)}${p.dataRientro ? ` al ${formatDate(p.dataRientro)}` : " (in corso)"}`).join("<br>");
  document.getElementById("schedaIscrizione").innerHTML = `<dl>
    ${riga("Iscritto dal", formatDate(i.dataIscrizione))}
    ${riga("Abbonamento", escapeHtml(i.tipoAbbonamentoNome))}
    ${riga("Quota", quota)}
    ${riga("Stagione", escapeHtml(i.stagioneNome))}
    ${riga("Periodi di ritiro", ritiri)}
    ${riga("Note", escapeHtml(i.note))}
  </dl>`;
}

function renderScadute(i, scadute) {
  const mie = scadute.filter(q => q.iscrizioneId === i.id);
  document.getElementById("pannelloScadute").hidden = mie.length === 0;
  document.getElementById("scaduteBody").innerHTML = mie.map(q => `
    <tr>
      <td><span class="pill pill-danger">${formatMese(q.mese)}</span></td>
      <td>${formatEuro(q.quotaImporto)}</td>
      <td class="cell-actions">
        <a class="btn btn-accent btn-sm" href="pagamento-form.html?studenteId=${q.studenteId}&iscrizioneId=${q.iscrizioneId}&mese=${q.mese}&ritorno=studente">Registra pagamento</a>
      </td>
    </tr>`).join("");
}

function renderPagamenti(i, pagamenti) {
  const miei = pagamenti.filter(p => p.iscrizioneId === i.id)
    .sort((a, b) => (a.dataPagamento < b.dataPagamento ? 1 : -1));
  document.getElementById("pagamentiBody").innerHTML = miei.length === 0
    ? `<tr><td colspan="3" class="empty-state">Nessun pagamento per questa iscrizione.</td></tr>`
    : miei.map(p => `
        <tr ${attributiRiga(`pagamento-form.html?id=${p.id}`)}>
          <td>${formatDate(p.dataPagamento)}</td>
          <td>${formatPeriodo(p.meseRiferimento, p.mesiCoperti)}</td>
          <td style="white-space:nowrap"><b>${formatEuro(p.importo)}</b></td>
        </tr>`).join("");
}

function renderPresenze(presenze) {
  const ordinate = [...presenze].sort((a, b) => (a.dataLezione < b.dataLezione ? 1 : -1));
  const presente = ordinate.filter(p => p.presente).length;
  document.getElementById("riepilogoPresenze").textContent =
    ordinate.length ? `${presente} presente su ${ordinate.length} lezioni registrate` : "";
  document.getElementById("presenzeBody").innerHTML = ordinate.length === 0
    ? `<tr><td colspan="2" class="empty-state">Nessuna presenza registrata.</td></tr>`
    : ordinate.slice(0, 12).map(p => `
        <tr>
          <td>${formatDate(p.dataLezione)}</td>
          <td>${p.presente ? '<span class="pill pill-success">Presente</span>' : '<span class="pill pill-muted">Assente</span>'}</td>
        </tr>`).join("");
}

async function eliminaIscrizione() {
  if (!await confermaEliminazione("questa iscrizione",
    "Se lo studente ha smesso di venire, usa invece \"Ritira\": così restano storico e pagamenti.")) return;
  try {
    await api.del(`/api/iscrizioni/${iscrizioneId}`);
    window.location.href = "iscrizioni.html";
  } catch (err) { showError(err.message); }
}

(async function () {
  if (!iscrizioneId) { window.location.href = "iscrizioni.html"; return; }
  try {
    const i = await api.get(`/api/iscrizioni/${iscrizioneId}`);
    document.title = `FullProject Studio - ${i.studenteNomeCompleto} · ${i.corsoNome}`;
    renderRelazione(i);
    renderIscrizione(i);
    const [studente, corso, scadute, pagamenti, presenze] = await Promise.all([
      api.get(`/api/studenti/${i.studenteId}`),
      api.get(`/api/corsi/${i.corsoId}`),
      api.get(`/api/pagamenti/quote/scadute?studenteId=${i.studenteId}`),
      api.get(`/api/pagamenti?studenteId=${i.studenteId}`),
      api.get(`/api/presenze?iscrizioneId=${i.id}`)
    ]);
    renderStudente(studente);
    renderCorso(corso);
    renderScadute(i, scadute);
    renderPagamenti(i, pagamenti);
    renderPresenze(presenze);
  } catch (err) {
    showError(err.message);
  }
})();
