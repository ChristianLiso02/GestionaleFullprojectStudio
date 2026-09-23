const METODI_PAGAMENTO = ["CONTANTI", "CARTA", "BONIFICO", "ALTRO"];
const STATI_PAGAMENTO = ["PAGATO", "IN_SOSPESO", "RIMBORSATO"];
const pagamentoId = new URLSearchParams(window.location.search).get("id");
const studenteIdIniziale = new URLSearchParams(window.location.search).get("studenteId");
const iscrizioneIdIniziale = new URLSearchParams(window.location.search).get("iscrizioneId");
const meseIniziale = new URLSearchParams(window.location.search).get("mese");
// "studente" = aperto dal dettaglio studente: al salvataggio si torna lì invece che alle quote del mese.
const tornaAlloStudente = new URLSearchParams(window.location.search).get("ritorno") === "studente";
let iscrizioniStudente = [];

async function loadStudenti() {
  const studenti = await api.get("/api/studenti");
  const studenteSel = document.getElementById("fStudente");
  studenti.forEach(s => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = `${s.nome} ${s.cognome}`;
    studenteSel.appendChild(opt);
  });

  document.getElementById("fMetodo").innerHTML = METODI_PAGAMENTO.map(m => `<option value="${m}">${m}</option>`).join("");
  document.getElementById("fStato").innerHTML = STATI_PAGAMENTO.map(s => `<option value="${s}">${s}</option>`).join("");
}

async function loadIscrizioniStudente(studenteId, iscrizioneSelezionata) {
  const iscrizioneSel = document.getElementById("fIscrizione");
  iscrizioneSel.innerHTML = "";

  if (!studenteId) {
    iscrizioneSel.innerHTML = `<option value="">-- seleziona prima uno studente --</option>`;
    iscrizioneSel.disabled = true;
    return;
  }

  iscrizioneSel.disabled = false;
  iscrizioneSel.innerHTML = `<option value="">-- nessuna --</option>`;
  try {
    const iscrizioni = await api.get(`/api/iscrizioni?studenteId=${studenteId}`);
    iscrizioniStudente = iscrizioni;
    iscrizioni.forEach(i => {
      const opt = document.createElement("option");
      opt.value = i.id;
      opt.textContent = `${i.corsoNome} (${i.stato})`;
      iscrizioneSel.appendChild(opt);
    });
    if (iscrizioneSelezionata) {
      iscrizioneSel.value = iscrizioneSelezionata;
    }
  } catch (err) {
    showError(err.message);
  }
}

function impostaMesi(n) {
  const sel = document.getElementById("fMesi");
  if (![...sel.options].some(o => o.value === String(n))) sel.add(new Option(`${n} mesi`, n));
  sel.value = String(n);
}

// Su un nuovo pagamento propone importo e mesi coperti dall'abbonamento dell'iscrizione scelta.
function precompilaDaIscrizione() {
  if (pagamentoId) return;
  const id = parseInt(document.getElementById("fIscrizione").value);
  const iscrizione = iscrizioniStudente.find(i => i.id === id);
  if (!iscrizione) return;
  if (iscrizione.quotaImporto != null) document.getElementById("fImporto").value = iscrizione.quotaImporto;
  impostaMesi(iscrizione.quotaMesi || 1);
  const causale = document.getElementById("fCausale");
  if (!causale.value) causale.value = `Quota ${iscrizione.corsoNome}`;
}

function fillForm(p) {
  document.getElementById("fStudente").value = p.studenteId || "";
  document.getElementById("fImporto").value = p.importo ?? "";
  document.getElementById("fData").value = p.dataPagamento || todayISO();
  document.getElementById("fMetodo").value = p.metodo || "CONTANTI";
  document.getElementById("fStato").value = p.stato || "PAGATO";
  document.getElementById("fCausale").value = p.causale || "";
  document.getElementById("fMese").value = p.meseRiferimento || "";
  impostaMesi(p.mesiCoperti || 1);
  document.getElementById("fNote").value = p.note || "";
}

function readForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    iscrizioneId: document.getElementById("fIscrizione").value ? parseInt(document.getElementById("fIscrizione").value) : null,
    importo: parseFloat(document.getElementById("fImporto").value) || 0,
    dataPagamento: document.getElementById("fData").value || todayISO(),
    meseRiferimento: document.getElementById("fMese").value || null,
    mesiCoperti: parseInt(document.getElementById("fMesi").value) || 1,
    metodo: document.getElementById("fMetodo").value,
    stato: document.getElementById("fStato").value,
    causale: document.getElementById("fCausale").value.trim(),
    note: document.getElementById("fNote").value.trim()
  };
}

async function init() {
  document.getElementById("fData").value = todayISO();
  document.getElementById("fMese").value = meseIniziale || todayISO().slice(0, 7);
  if (tornaAlloStudente) document.getElementById("linkAnnulla").href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
  else if (meseIniziale) document.getElementById("linkAnnulla").href = `quote.html?mese=${meseIniziale}`;
  try {
    await loadStudenti();

    if (pagamentoId) {
      document.getElementById("formTitle").textContent = "Modifica pagamento";
      const p = await api.get(`/api/pagamenti/${pagamentoId}`);
      fillForm(p);
      await loadIscrizioniStudente(p.studenteId, p.iscrizioneId);
    } else if (studenteIdIniziale) {
      document.getElementById("fStudente").value = studenteIdIniziale;
      await loadIscrizioniStudente(studenteIdIniziale, iscrizioneIdIniziale);
      precompilaDaIscrizione();
    } else {
      await loadIscrizioniStudente(null);
    }
  } catch (err) {
    showError(err.message);
  }
}

document.getElementById("fStudente").addEventListener("change", (e) => {
  loadIscrizioniStudente(e.target.value || null);
});

document.getElementById("fIscrizione").addEventListener("change", precompilaDaIscrizione);

document.getElementById("pagamentoForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("pagamentoForm"));
  if (!dto.studenteId) { setFieldError("fStudente", "Lo studente è obbligatorio."); return; }
  if (!dto.meseRiferimento) { setFieldError("fMese", "Indica il mese a cui si riferisce il pagamento."); return; }
  try {
    if (pagamentoId) {
      await api.put(`/api/pagamenti/${pagamentoId}`, dto);
    } else {
      await api.post("/api/pagamenti", dto);
    }
    if (tornaAlloStudente) {
      window.location.href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
    } else if (meseIniziale) {
      window.location.href = `quote.html?mese=${meseIniziale}`;
    } else if (studenteIdIniziale) {
      window.location.href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
    } else {
      window.location.href = "pagamenti.html";
    }
  } catch (err) {
    showError(err.message);
  }
});

init();
