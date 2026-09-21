const STILI_BALLO = ["SALSA_CUBANA", "SALSA_LOS_ANGELES", "SALSA_PORTORICANA", "RUEDA_DE_CASINO", "BACHATA", "KIZOMBA", "CHACHACHA", "ALTRO"];
const istruttoreId = new URLSearchParams(window.location.search).get("id");

function fillSpecializzazioniOptions() {
  document.getElementById("specializzazioniContainer").innerHTML = STILI_BALLO.map(s =>
    `<label><input type="checkbox" value="${s}" class="fSpec"> ${s}</label>`
  ).join("");
}

function fillForm(i) {
  document.getElementById("fNome").value = i.nome || "";
  document.getElementById("fCognome").value = i.cognome || "";
  document.getElementById("fTelefono").value = i.telefono || "";
  document.getElementById("fEmail").value = i.email || "";
  document.getElementById("fCompenso").value = i.compensoOrario ?? "";
  document.getElementById("fAttivo").checked = i.attivo !== false;
  const spec = new Set(i.specializzazioni || []);
  document.querySelectorAll(".fSpec").forEach(el => { el.checked = spec.has(el.value); });
}

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    cognome: document.getElementById("fCognome").value.trim(),
    telefono: document.getElementById("fTelefono").value.trim(),
    email: document.getElementById("fEmail").value.trim(),
    compensoOrario: document.getElementById("fCompenso").value ? parseFloat(document.getElementById("fCompenso").value) : null,
    specializzazioni: Array.from(document.querySelectorAll(".fSpec:checked")).map(el => el.value),
    attivo: document.getElementById("fAttivo").checked
  };
}

function validaForm(dto) {
  clearAllFieldErrors(document.getElementById("istruttoreForm"));
  let ok = true;

  if (!dto.nome) { setFieldError("fNome", "Il nome è obbligatorio."); ok = false; }
  if (!dto.cognome) { setFieldError("fCognome", "Il cognome è obbligatorio."); ok = false; }

  const errTel = validateTelefono(dto.telefono);
  if (errTel) { setFieldError("fTelefono", errTel); ok = false; }

  const errEmail = validateEmail(dto.email);
  if (errEmail) { setFieldError("fEmail", errEmail); ok = false; }

  return ok;
}

async function init() {
  fillSpecializzazioniOptions();
  if (istruttoreId) {
    document.getElementById("formTitle").textContent = "Modifica istruttore";
    try {
      const i = await api.get(`/api/istruttori/${istruttoreId}`);
      fillForm(i);
    } catch (err) {
      showError(err.message);
    }
  }
}

document.getElementById("istruttoreForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  if (!validaForm(dto)) return;
  try {
    if (istruttoreId) {
      await api.put(`/api/istruttori/${istruttoreId}`, dto);
    } else {
      await api.post("/api/istruttori", dto);
    }
    window.location.href = "istruttori.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
