const STATI_ISCRIZIONE = ["ATTIVA", "SCADUTA", "ANNULLATA"];
const iscrizioneId = new URLSearchParams(window.location.search).get("id");
const studenteIdIniziale = new URLSearchParams(window.location.search).get("studenteId");
const corsoIdIniziale = new URLSearchParams(window.location.search).get("corsoId");

async function loadLookups() {
  const [studenti, corsi, abbonamenti] = await Promise.all([
    api.get("/api/studenti"),
    api.get("/api/corsi"),
    api.get("/api/abbonamenti")
  ]);

  const studenteSel = document.getElementById("fStudente");
  studenti.forEach(s => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = `${s.nome} ${s.cognome}`;
    studenteSel.appendChild(opt);
  });

  const corsoSel = document.getElementById("fCorso");
  corsi.forEach(c => {
    const opt = document.createElement("option");
    opt.value = c.id;
    opt.textContent = c.nome;
    corsoSel.appendChild(opt);
  });

  const abbonamentoSel = document.getElementById("fAbbonamento");
  abbonamenti.forEach(a => {
    const opt = document.createElement("option");
    opt.value = a.id;
    opt.textContent = a.nome;
    abbonamentoSel.appendChild(opt);
  });

  document.getElementById("fStato").innerHTML = STATI_ISCRIZIONE.map(s => `<option value="${s}">${s}</option>`).join("");
}

function fillForm(i) {
  document.getElementById("fStudente").value = i.studenteId || "";
  document.getElementById("fCorso").value = i.corsoId || "";
  document.getElementById("fAbbonamento").value = i.tipoAbbonamentoId || "";
  document.getElementById("fDataIscr").value = i.dataIscrizione || todayISO();
  document.getElementById("fDataScad").value = i.dataScadenza || "";
  document.getElementById("fStato").value = i.stato || "ATTIVA";
  document.getElementById("fNote").value = i.note || "";
}

function readForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    corsoId: parseInt(document.getElementById("fCorso").value),
    tipoAbbonamentoId: document.getElementById("fAbbonamento").value ? parseInt(document.getElementById("fAbbonamento").value) : null,
    dataIscrizione: document.getElementById("fDataIscr").value || todayISO(),
    dataScadenza: document.getElementById("fDataScad").value || null,
    stato: document.getElementById("fStato").value,
    note: document.getElementById("fNote").value.trim()
  };
}

async function init() {
  document.getElementById("fDataIscr").value = todayISO();
  try {
    await loadLookups();
    if (iscrizioneId) {
      document.getElementById("formTitle").textContent = "Modifica iscrizione";
      const i = await api.get(`/api/iscrizioni/${iscrizioneId}`);
      fillForm(i);
    } else {
      if (studenteIdIniziale) document.getElementById("fStudente").value = studenteIdIniziale;
      if (corsoIdIniziale) document.getElementById("fCorso").value = corsoIdIniziale;
    }
  } catch (err) {
    showError(err.message);
  }
}

document.getElementById("iscrizioneForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  if (!dto.studenteId || !dto.corsoId) { showError("Studente e corso sono obbligatori."); return; }
  try {
    if (iscrizioneId) {
      await api.put(`/api/iscrizioni/${iscrizioneId}`, dto);
    } else {
      await api.post("/api/iscrizioni", dto);
    }
    if (studenteIdIniziale) {
      window.location.href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
    } else if (corsoIdIniziale) {
      window.location.href = `corso-dettaglio.html?id=${corsoIdIniziale}`;
    } else {
      window.location.href = "iscrizioni.html";
    }
  } catch (err) {
    showError(err.message);
  }
});

init();
