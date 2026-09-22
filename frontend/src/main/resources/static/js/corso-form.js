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
  const [istruttori, sale, stagioni] = await Promise.all([api.get("/api/istruttori"), api.get("/api/sale"), api.get("/api/stagioni")]);
  [document.getElementById("fIstruttore1"), document.getElementById("fIstruttore2")].forEach(sel => {
    istruttori.forEach(i => {
      const opt = document.createElement("option");
      opt.value = i.id;
      opt.textContent = `${i.nome} ${i.cognome}`;
      sel.appendChild(opt);
    });
  });
  const salaSel = document.getElementById("fSala");
  sale.forEach(s => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = s.nome;
    salaSel.appendChild(opt);
  });
  const stagioneSel = document.getElementById("fStagione");
  stagioneSel.innerHTML = stagioni.map(s => `<option value="${s.id}">${escapeHtml(s.nome)}${s.corrente ? " (corrente)" : ""}</option>`).join("");
  const corrente = stagioni.find(s => s.corrente);
  if (corrente) stagioneSel.value = corrente.id;
}

function fillForm(c) {
  document.getElementById("fNome").value = c.nome || "";
  document.getElementById("fStile").value = c.stile || "SALSA_CUBANA";
  document.getElementById("fLivello").value = c.livello || "BASE";
  const istruttoriIds = c.istruttoriIds || [];
  document.getElementById("fIstruttore1").value = istruttoriIds[0] || "";
  document.getElementById("fIstruttore2").value = istruttoriIds[1] || "";
  document.getElementById("fSala").value = c.salaId || "";
  if (c.stagioneId) document.getElementById("fStagione").value = c.stagioneId;
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
    istruttoriIds: [document.getElementById("fIstruttore1").value, document.getElementById("fIstruttore2").value]
      .filter(v => v)
      .map(v => parseInt(v))
      .filter((v, idx, arr) => arr.indexOf(v) === idx),
    salaId: document.getElementById("fSala").value ? parseInt(document.getElementById("fSala").value) : null,
    stagioneId: document.getElementById("fStagione").value ? parseInt(document.getElementById("fStagione").value) : null,
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
  const i1 = document.getElementById("fIstruttore1").value;
  const i2 = document.getElementById("fIstruttore2").value;
  if (i1 && i2 && i1 === i2) { showError("Non puoi selezionare lo stesso istruttore due volte."); return; }
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
