const STILI_BALLO = ["SALSA_CUBANA", "SALSA_LOS_ANGELES", "SALSA_PORTORICANA", "RUEDA_DE_CASINO", "BACHATA", "KIZOMBA", "CHACHACHA", "ALTRO"];
const LIVELLI = ["BASE", "INTERMEDIO", "AVANZATO", "TUTTI_I_LIVELLI"];
const GIORNI = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const GIORNI_LABEL = { MONDAY: "Lun", TUESDAY: "Mar", WEDNESDAY: "Mer", THURSDAY: "Gio", FRIDAY: "Ven", SATURDAY: "Sab", SUNDAY: "Dom" };

const corsoId = new URLSearchParams(window.location.search).get("id");

function fillSelectOptions() {
  document.getElementById("fStile").innerHTML = STILI_BALLO.map(s => `<option value="${s}">${s}</option>`).join("");
  document.getElementById("fLivello").innerHTML = LIVELLI.map(l => `<option value="${l}">${l}</option>`).join("");
  document.getElementById("giorniContainer").innerHTML = GIORNI.map(g =>
    `<label><input type="checkbox" value="${g}" class="fGiorno"> ${GIORNI_LABEL[g]}</label>`
  ).join("");
}

async function loadLookups() {
  const [istruttori, sale] = await Promise.all([api.get("/api/istruttori"), api.get("/api/sale")]);
  const istruttoreSel = document.getElementById("fIstruttore");
  istruttori.forEach(i => {
    const opt = document.createElement("option");
    opt.value = i.id;
    opt.textContent = `${i.nome} ${i.cognome}`;
    istruttoreSel.appendChild(opt);
  });
  const salaSel = document.getElementById("fSala");
  sale.forEach(s => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = s.nome;
    salaSel.appendChild(opt);
  });
}

function fillForm(c) {
  document.getElementById("fNome").value = c.nome || "";
  document.getElementById("fStile").value = c.stile || "SALSA_CUBANA";
  document.getElementById("fLivello").value = c.livello || "BASE";
  document.getElementById("fIstruttore").value = c.istruttoreId || "";
  document.getElementById("fSala").value = c.salaId || "";
  document.getElementById("fCapienza").value = c.capienzaMax ?? "";
  document.getElementById("fOrarioInizio").value = c.orarioInizio || "";
  document.getElementById("fOrarioFine").value = c.orarioFine || "";
  document.getElementById("fPrezzo").value = c.prezzoMensile ?? "";
  document.getElementById("fDataInizio").value = c.dataInizio || "";
  document.getElementById("fAttivo").checked = c.attivo !== false;
  const giorni = new Set(c.giorniSettimana || []);
  document.querySelectorAll(".fGiorno").forEach(el => { el.checked = giorni.has(el.value); });
}

function readForm() {
  return {
    nome: document.getElementById("fNome").value.trim(),
    stile: document.getElementById("fStile").value,
    livello: document.getElementById("fLivello").value,
    istruttoreId: document.getElementById("fIstruttore").value ? parseInt(document.getElementById("fIstruttore").value) : null,
    salaId: document.getElementById("fSala").value ? parseInt(document.getElementById("fSala").value) : null,
    capienzaMax: document.getElementById("fCapienza").value ? parseInt(document.getElementById("fCapienza").value) : null,
    orarioInizio: document.getElementById("fOrarioInizio").value || null,
    orarioFine: document.getElementById("fOrarioFine").value || null,
    prezzoMensile: document.getElementById("fPrezzo").value ? parseFloat(document.getElementById("fPrezzo").value) : null,
    dataInizio: document.getElementById("fDataInizio").value || null,
    giorniSettimana: Array.from(document.querySelectorAll(".fGiorno:checked")).map(el => el.value),
    attivo: document.getElementById("fAttivo").checked
  };
}

async function init() {
  fillSelectOptions();
  try {
    await loadLookups();
    if (corsoId) {
      document.getElementById("formTitle").textContent = "Modifica corso";
      const c = await api.get(`/api/corsi/${corsoId}`);
      fillForm(c);
    }
  } catch (err) {
    showError(err.message);
  }
}

document.getElementById("corsoForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const dto = readForm();
  if (!dto.nome) { showError("Il nome è obbligatorio."); return; }
  try {
    if (corsoId) {
      await api.put(`/api/corsi/${corsoId}`, dto);
    } else {
      await api.post("/api/corsi", dto);
    }
    window.location.href = "corsi.html";
  } catch (err) {
    showError(err.message);
  }
});

init();
