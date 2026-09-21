const METODI_PAGAMENTO = ["CONTANTI", "CARTA", "BONIFICO", "ALTRO"];
const STATI_PAGAMENTO = ["PAGATO", "IN_SOSPESO", "RIMBORSATO"];
const pagamentoId = new URLSearchParams(window.location.search).get("id");
const studenteIdIniziale = new URLSearchParams(window.location.search).get("studenteId");

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

function fillForm(p) {
  document.getElementById("fStudente").value = p.studenteId || "";
  document.getElementById("fImporto").value = p.importo ?? "";
  document.getElementById("fData").value = p.dataPagamento || todayISO();
  document.getElementById("fMetodo").value = p.metodo || "CONTANTI";
  document.getElementById("fStato").value = p.stato || "PAGATO";
  document.getElementById("fCausale").value = p.causale || "";
  document.getElementById("fNote").value = p.note || "";
}

function readForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    iscrizioneId: document.getElementById("fIscrizione").value ? parseInt(document.getElementById("fIscrizione").value) : null,
    importo: parseFloat(document.getElementById("fImporto").value) || 0,
    dataPagamento: document.getElementById("fData").value || todayISO(),
    metodo: document.getElementById("fMetodo").value,
    stato: document.getElementById("fStato").value,
    causale: document.getElementById("fCausale").value.trim(),
    note: document.getElementById("fNote").value.trim()
  };
}

async function init() {
  document.getElementById("fData").value = todayISO();
  try {
    await loadStudenti();

    if (pagamentoId) {
      document.getElementById("formTitle").textContent = "Modifica pagamento";
      const p = await api.get(`/api/pagamenti/${pagamentoId}`);
      fillForm(p);
      await loadIscrizioniStudente(p.studenteId, p.iscrizioneId);
    } else if (studenteIdIniziale) {
      document.getElementById("fStudente").value = studenteIdIniziale;
      await loadIscrizioniStudente(studenteIdIniziale);
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

document.getElementById("pagamentoForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("pagamentoForm"));
  if (!dto.studenteId) { setFieldError("fStudente", "Lo studente è obbligatorio."); return; }
  try {
    if (pagamentoId) {
      await api.put(`/api/pagamenti/${pagamentoId}`, dto);
    } else {
      await api.post("/api/pagamenti", dto);
    }
    if (studenteIdIniziale) {
      window.location.href = `studente-dettaglio.html?id=${studenteIdIniziale}`;
    } else {
      window.location.href = "pagamenti.html";
    }
  } catch (err) {
    showError(err.message);
  }
});

init();
