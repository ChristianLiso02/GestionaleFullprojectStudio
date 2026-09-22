const stagioneId = new URLSearchParams(window.location.search).get("id");

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    dataInizio: document.getElementById("fDataInizio").value || null,
    dataFine: document.getElementById("fDataFine").value || null,
    corrente: document.getElementById("fCorrente").checked
  };
}

async function init() {
  if (stagioneId) {
    document.getElementById("formTitle").textContent = "Modifica stagione";
    // La stagione corrente si cambia solo dal pulsante "Rendi corrente" nella lista.
    document.getElementById("campoCorrente").hidden = true;
    try {
      const s = await api.get(`/api/stagioni/${stagioneId}`);
      document.getElementById("fNome").value = s.nome || "";
      document.getElementById("fDataInizio").value = s.dataInizio || "";
      document.getElementById("fDataFine").value = s.dataFine || "";
    } catch (err) {
      showError(err.message);
    }
  }
}

document.getElementById("stagioneForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("stagioneForm"));
  if (!dto.nome) { setFieldError("fNome", "Il nome è obbligatorio."); return; }
  try {
    if (stagioneId) {
      await api.put(`/api/stagioni/${stagioneId}`, dto);
    } else {
      await api.post("/api/stagioni", dto);
    }
    window.location.href = "stagioni.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
