const abbonamentoId = new URLSearchParams(window.location.search).get("id");

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    prezzo: parseFloat(document.getElementById("fPrezzo").value) || 0,
    durataGiorni: document.getElementById("fDurata").value ? parseInt(document.getElementById("fDurata").value) : null,
    numeroLezioni: document.getElementById("fLezioni").value ? parseInt(document.getElementById("fLezioni").value) : null,
    descrizione: document.getElementById("fDescrizione").value.trim(),
    attivo: document.getElementById("fAttivo").checked
  };
}

async function init() {
  if (abbonamentoId) {
    document.getElementById("formTitle").textContent = "Modifica abbonamento";
    try {
      const a = await api.get(`/api/abbonamenti/${abbonamentoId}`);
      document.getElementById("fNome").value = a.nome || "";
      document.getElementById("fPrezzo").value = a.prezzo ?? "";
      document.getElementById("fDurata").value = a.durataGiorni ?? "";
      document.getElementById("fLezioni").value = a.numeroLezioni ?? "";
      document.getElementById("fDescrizione").value = a.descrizione || "";
      document.getElementById("fAttivo").checked = a.attivo !== false;
    } catch (err) {
      showError(err.message);
    }
  }
}

document.getElementById("abbonamentoForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("abbonamentoForm"));
  if (!dto.nome) { setFieldError("fNome", "Il nome è obbligatorio."); return; }
  try {
    if (abbonamentoId) {
      await api.put(`/api/abbonamenti/${abbonamentoId}`, dto);
    } else {
      await api.post("/api/abbonamenti", dto);
    }
    window.location.href = "abbonamenti.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
