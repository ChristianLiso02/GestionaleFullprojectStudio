const salaId = new URLSearchParams(window.location.search).get("id");

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    capienza: document.getElementById("fCapienza").value ? parseInt(document.getElementById("fCapienza").value) : null,
    note: document.getElementById("fNote").value.trim()
  };
}

async function init() {
  if (salaId) {
    document.getElementById("formTitle").textContent = "Modifica sala";
    try {
      const s = await api.get(`/api/sale/${salaId}`);
      document.getElementById("fNome").value = s.nome || "";
      document.getElementById("fCapienza").value = s.capienza ?? "";
      document.getElementById("fNote").value = s.note || "";
    } catch (err) {
      showError(err.message);
    }
  }
}

document.getElementById("salaForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  clearAllFieldErrors(document.getElementById("salaForm"));
  if (!dto.nome) { setFieldError("fNome", "Il nome è obbligatorio."); return; }
  try {
    if (salaId) {
      await api.put(`/api/sale/${salaId}`, dto);
    } else {
      await api.post("/api/sale", dto);
    }
    window.location.href = "sale.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
