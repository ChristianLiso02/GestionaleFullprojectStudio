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
}

function fillForm(i) {
  document.getElementById("fStudente").value = i.studenteId || "";
  document.getElementById("fCorso").value = i.corsoId || "";
  document.getElementById("fAbbonamento").value = i.tipoAbbonamentoId || "";
  document.getElementById("fDataIscr").value = i.dataIscrizione || todayISO();
  document.getElementById("fNote").value = i.note || "";
}

function renderRitiri(i) {
  const ritiri = i.ritiri || [];
  document.getElementById("pannelloRitiri").hidden = ritiri.length === 0;
  document.getElementById("ritiriBody").innerHTML = ritiri.map(p => `
    <tr>
      <td>${formatDate(p.dataRitiro)}</td>
      <td>${p.dataRientro ? formatDate(p.dataRientro) : '<span class="pill pill-muted">ancora ritirato</span>'}</td>
      <td class="cell-actions"><button type="button" class="btn btn-ghost btn-sm" onclick="annullaRitiro(${p.id})">Annulla ritiro</button></td>
    </tr>
  `).join("");
}

async function annullaRitiro(periodoId) {
  if (!confirm("Annullare questo periodo di ritiro? I mesi che copriva torneranno dovuti.")) return;
  try {
    const i = await api.del(`/api/iscrizioni/${iscrizioneId}/ritiri/${periodoId}`);
    renderRitiri(i);
  } catch (err) { showError(err.message); }
}

function readForm() {
  return {
    studenteId: parseInt(document.getElementById("fStudente").value),
    corsoId: parseInt(document.getElementById("fCorso").value),
    tipoAbbonamentoId: document.getElementById("fAbbonamento").value ? parseInt(document.getElementById("fAbbonamento").value) : null,
    dataIscrizione: document.getElementById("fDataIscr").value || todayISO(),
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
      renderRitiri(i);
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
