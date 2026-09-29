const METODI_PAGAMENTO = ["CONTANTI", "CARTA", "BONIFICO", "ALTRO"];
const pagamentoId = new URLSearchParams(window.location.search).get("id");
const studenteIdIniziale = new URLSearchParams(window.location.search).get("studenteId");
const iscrizioneIdIniziale = new URLSearchParams(window.location.search).get("iscrizioneId");
const meseIniziale = new URLSearchParams(window.location.search).get("mese");
// Aperto da "+ Lezione singola" nel dettaglio corso: tipo e corso già scelti, al salvataggio si torna al corso.
const corsoIdIniziale = new URLSearchParams(window.location.search).get("corsoId");
// "studente" = aperto dal dettaglio studente: al salvataggio si torna lì invece che alle quote del mese.
const tornaAlloStudente = new URLSearchParams(window.location.search).get("ritorno") === "studente";
let iscrizioniStudente = [];
let corsiLezione = [];

function tipoScelto() {
  return document.querySelector('input[name="tipo"]:checked').value;
}

function mostraTipo() {
  const lezione = tipoScelto() === "LEZIONE_SINGOLA";
  document.getElementById("bloccoLezione").hidden = !lezione;
  document.getElementById("bloccoIscrizione").hidden = lezione;
  document.getElementById("bloccoMesi").hidden = lezione;
}

function impostaTipo(tipo) {
  document.querySelector(`input[name="tipo"][value="${tipo}"]`).checked = true;
  mostraTipo();
}

// Corsi della stagione corrente; se si modifica una lezione di un altro corso, lo si aggiunge alla lista.
async function loadCorsi(corsoSalvato) {
  const stagioni = await api.get("/api/stagioni");
  const corrente = stagioni.find(s => s.corrente);
  const corsi = await api.get(corrente ? `/api/corsi?stagioneId=${corrente.id}` : "/api/corsi");
  corsiLezione = corsi.filter(c => c.attivo || (corsoSalvato && c.id === corsoSalvato.id))
    .sort((a, b) => a.nome.localeCompare(b.nome, "it"));
  const sel = document.getElementById("fCorso");
  sel.innerHTML = `<option value="">-- seleziona --</option>` +
    corsiLezione.map(c => `<option value="${c.id}">${escapeHtml(c.nome)}</option>`).join("");
  if (corsoSalvato && !corsiLezione.some(c => c.id === corsoSalvato.id)) {
    sel.add(new Option(corsoSalvato.nome, corsoSalvato.id));
  }
}

// Propone il prezzo della lezione singola del corso e una causale, se non già scritta a mano.
function precompilaDaCorso() {
  const corso = corsiLezione.find(c => c.id === parseInt(document.getElementById("fCorso").value));
  if (!corso) return;
  if (corso.prezzoLezioneSingola != null) document.getElementById("fImporto").value = corso.prezzoLezioneSingola;
  const causale = document.getElementById("fCausale");
  if (!causale.value || causale.value.startsWith("Lezione singola") || causale.value.startsWith("Quota ")) {
    causale.value = `Lezione singola ${corso.nome}`;
  }
}

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
  impostaTipo(p.tipo || "QUOTA_CORSO");
  document.getElementById("fCorso").value = p.tipo === "LEZIONE_SINGOLA" ? p.corsoId : "";
  document.getElementById("fDataLezione").value = p.dataLezione || "";
  document.getElementById("fStudente").value = p.studenteId || "";
  document.getElementById("fImporto").value = p.importo ?? "";
  document.getElementById("fData").value = p.dataPagamento || todayISO();
  document.getElementById("fMetodo").value = p.metodo || "CONTANTI";
  if (p.stato && p.stato !== "PAGATO") {
    const nome = p.stato === "IN_SOSPESO" ? "in sospeso" : "rimborsato";
    const avviso = document.getElementById("avvisoStato");
    avviso.textContent = `Questo pagamento era segnato come "${nome}" e per ora non conta come incasso. ` +
      `Se lo salvi diventa un pagamento effettuato; se i soldi non sono stati ricevuti (o sono stati restituiti), eliminalo dalla lista Pagamenti.`;
    avviso.hidden = false;
  }
  document.getElementById("fCausale").value = p.causale || "";
  document.getElementById("fMese").value = p.meseRiferimento || "";
  impostaMesi(p.mesiCoperti || 1);
  document.getElementById("fNote").value = p.note || "";
}

function readForm() {
  const lezione = tipoScelto() === "LEZIONE_SINGOLA";
  const dataLezione = document.getElementById("fDataLezione").value || null;
  return {
    tipo: tipoScelto(),
    studenteId: parseInt(document.getElementById("fStudente").value),
    iscrizioneId: !lezione && document.getElementById("fIscrizione").value ? parseInt(document.getElementById("fIscrizione").value) : null,
    corsoId: lezione && document.getElementById("fCorso").value ? parseInt(document.getElementById("fCorso").value) : null,
    dataLezione: lezione ? dataLezione : null,
    importo: parseFloat(document.getElementById("fImporto").value) || 0,
    dataPagamento: document.getElementById("fData").value || todayISO(),
    // Per la lezione singola il mese è quello della lezione (lo ricalcola comunque il server).
    meseRiferimento: lezione ? (dataLezione ? dataLezione.slice(0, 7) : null) : (document.getElementById("fMese").value || null),
    mesiCoperti: lezione ? 1 : (parseInt(document.getElementById("fMesi").value) || 1),
    metodo: document.getElementById("fMetodo").value,
    stato: "PAGATO",
    causale: document.getElementById("fCausale").value.trim(),
    note: document.getElementById("fNote").value.trim()
  };
}

async function init() {
  document.getElementById("fData").value = todayISO();
  document.getElementById("fMese").value = meseIniziale || todayISO().slice(0, 7);
  document.getElementById("fDataLezione").value = todayISO();
  if (corsoIdIniziale) document.getElementById("linkAnnulla").href = `corso-dettaglio.html?id=${corsoIdIniziale}`;
  else if (tornaAlloStudente) document.getElementById("linkAnnulla").href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
  else if (meseIniziale) document.getElementById("linkAnnulla").href = `quote.html?mese=${meseIniziale}`;
  try {
    await loadStudenti();

    if (pagamentoId) {
      document.getElementById("formTitle").textContent = "Modifica pagamento";
      const p = await api.get(`/api/pagamenti/${pagamentoId}`);
      await loadCorsi(p.tipo === "LEZIONE_SINGOLA" ? { id: p.corsoId, nome: p.corsoNome } : null);
      fillForm(p);
      await loadIscrizioniStudente(p.studenteId, p.iscrizioneId);
      return;
    }
    await loadCorsi(null);
    if (corsoIdIniziale) {
      impostaTipo("LEZIONE_SINGOLA");
      document.getElementById("fCorso").value = corsoIdIniziale;
      precompilaDaCorso();
      await loadIscrizioniStudente(null);
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
document.getElementById("fCorso").addEventListener("change", precompilaDaCorso);
document.querySelectorAll('input[name="tipo"]').forEach(r => r.addEventListener("change", () => {
  mostraTipo();
  if (tipoScelto() === "LEZIONE_SINGOLA") precompilaDaCorso();
  else precompilaDaIscrizione();
}));

document.getElementById("pagamentoForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("pagamentoForm"));
  if (!dto.studenteId) { setFieldError("fStudente", "Lo studente è obbligatorio."); return; }
  if (dto.tipo === "LEZIONE_SINGOLA") {
    if (!dto.corsoId) { setFieldError("fCorso", "Scegli il corso della lezione."); return; }
    if (!dto.dataLezione) { setFieldError("fDataLezione", "Indica il giorno della lezione."); return; }
  } else if (!dto.meseRiferimento) { setFieldError("fMese", "Indica il mese a cui si riferisce il pagamento."); return; }
  try {
    if (pagamentoId) {
      await api.put(`/api/pagamenti/${pagamentoId}`, dto);
    } else {
      await api.post("/api/pagamenti", dto);
    }
    if (corsoIdIniziale) {
      window.location.href = `corso-dettaglio.html?id=${corsoIdIniziale}`;
    } else if (tornaAlloStudente) {
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
