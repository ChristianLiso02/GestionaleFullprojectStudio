let corsiLookupPresenze = [];
const corsoIdPreselezionato = new URLSearchParams(window.location.search).get("corsoId");

async function initPresenze() {
  try {
    corsiLookupPresenze = await api.get("/api/corsi");
    const select = document.getElementById("corsoSelect");
    select.innerHTML = `<option value="">-- seleziona un corso --</option>` +
      corsiLookupPresenze.map(c => `<option value="${c.id}">${escapeHtml(c.nome)}</option>`).join("");

    document.getElementById("dataInput").value = todayISO();

    select.addEventListener("change", loadAppello);
    document.getElementById("dataInput").addEventListener("change", loadAppello);

    if (corsoIdPreselezionato) {
      select.value = corsoIdPreselezionato;
      await loadAppello();
    }
  } catch (err) {
    showError(err.message);
  }
}

async function loadAppello() {
  const corsoId = document.getElementById("corsoSelect").value;
  const data = document.getElementById("dataInput").value;
  const container = document.getElementById("presenzeContainer");

  if (!corsoId) {
    container.innerHTML = `<div class="empty-state">Seleziona un corso per registrare le presenze.</div>`;
    return;
  }

  try {
    const [iscrizioni, presenze] = await Promise.all([
      api.get(`/api/iscrizioni?corsoId=${corsoId}`),
      api.get(`/api/presenze?corsoId=${corsoId}&data=${data}`)
    ]);

    const iscritti = iscrizioni.filter(i => i.stato === "ATTIVA");
    const presenzeMap = {};
    presenze.forEach(p => { presenzeMap[p.iscrizioneId] = p.presente; });

    container.innerHTML = iscritti.length === 0
      ? `<div class="empty-state">Nessuno studente iscritto a questo corso.</div>`
      : iscritti.map(i => `
          <div class="check-row">
            <span>${escapeHtml(i.studenteNomeCompleto)}</span>
            <input type="checkbox" data-iscrizione="${i.id}" ${presenzeMap[i.id] ? "checked" : ""} onchange="registraPresenza(${i.id}, this.checked)">
          </div>
        `).join("");
  } catch (err) {
    showError(err.message);
  }
}

async function registraPresenza(iscrizioneId, presente) {
  const data = document.getElementById("dataInput").value;
  try {
    await api.post("/api/presenze", { iscrizioneId, dataLezione: data, presente });
  } catch (err) {
    showError(err.message);
  }
}

initPresenze();
