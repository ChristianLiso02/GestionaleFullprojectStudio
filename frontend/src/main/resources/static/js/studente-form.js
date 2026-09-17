const studenteId = new URLSearchParams(window.location.search).get("id");

function fillForm(s) {
  document.getElementById("fNome").value = s.nome || "";
  document.getElementById("fCognome").value = s.cognome || "";
  document.getElementById("fCf").value = s.codiceFiscale || "";
  document.getElementById("fNascita").value = s.dataNascita || "";
  document.getElementById("fTelefono").value = s.telefono || "";
  document.getElementById("fEmail").value = s.email || "";
  document.getElementById("fIndirizzo").value = s.indirizzo || "";
  document.getElementById("fEmergenza").value = s.contattoEmergenza || "";
  document.getElementById("fDataIscr").value = s.dataIscrizione || todayISO();
  document.getElementById("fNote").value = s.noteMediche || "";
  document.getElementById("fAttivo").checked = s.attivo !== false;
}

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    cognome: document.getElementById("fCognome").value.trim(),
    codiceFiscale: document.getElementById("fCf").value.trim(),
    dataNascita: document.getElementById("fNascita").value || null,
    telefono: document.getElementById("fTelefono").value.trim(),
    email: document.getElementById("fEmail").value.trim(),
    indirizzo: document.getElementById("fIndirizzo").value.trim(),
    contattoEmergenza: document.getElementById("fEmergenza").value.trim(),
    dataIscrizione: document.getElementById("fDataIscr").value || todayISO(),
    noteMediche: document.getElementById("fNote").value.trim(),
    attivo: document.getElementById("fAttivo").checked
  };
}

async function init() {
  document.getElementById("fDataIscr").value = todayISO();

  if (studenteId) {
    document.getElementById("formTitle").textContent = "Modifica studente";
    try {
      const s = await api.get(`/api/studenti/${studenteId}`);
      fillForm(s);
    } catch (err) {
      showError(err.message);
    }
  }
}

document.getElementById("studenteForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  if (!dto.nome || !dto.cognome) { showError("Nome e cognome sono obbligatori."); return; }
  try {
    if (studenteId) {
      await api.put(`/api/studenti/${studenteId}`, dto);
    } else {
      await api.post("/api/studenti", dto);
    }
    window.location.href = "studenti.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
