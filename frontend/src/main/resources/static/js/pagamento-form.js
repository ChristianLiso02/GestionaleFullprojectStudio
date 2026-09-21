const METODI_PAGAMENTO = ["CONTANTI", "CARTA", "BONIFICO", "ALTRO"];
const STATI_PAGAMENTO = ["PAGATO", "IN_SOSPESO", "RIMBORSATO"];
const pagamentoId = new URLSearchParams(window.location.search).get("id");

async function loadLookups() {
  const [studenti, iscrizioni] = await Promise.all([api.get("/api/studenti"), api.get("/api/iscrizioni")]);

  const studenteSel = document.getElementById("fStudente");
  studenti.forEach(s => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = `${s.nome} ${s.cognome}`;
    studenteSel.appendChild(opt);
  });

  const iscrizioneSel = document.getElementById("fIscrizione");
  iscrizioni.forEach(i => {
    const opt = document.createElement("option");
    opt.value = i.id;
    opt.textContent = `${i.studenteNomeCompleto} - ${i.corsoNome}`;
    iscrizioneSel.appendChild(opt);
  });

  document.getElementById("fMetodo").innerHTML = METODI_PAGAMENTO.map(m => `<option value="${m}">${m}</option>`).join("");
  document.getElementById("fStato").innerHTML = STATI_PAGAMENTO.map(s => `<option value="${s}">${s}</option>`).join("");
}

function fillForm(p) {
  document.getElementById("fStudente").value = p.studenteId || "";
  document.getElementById("fIscrizione").value = p.iscrizioneId || "";
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
    await loadLookups();
    if (pagamentoId) {
      document.getElementById("formTitle").textContent = "Modifica pagamento";
      const p = await api.get(`/api/pagamenti/${pagamentoId}`);
      fillForm(p);
    }
  } catch (err) {
    showError(err.message);
  }
}

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
    window.location.href = "pagamenti.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
