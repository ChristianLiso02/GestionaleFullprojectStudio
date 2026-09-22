document.getElementById("passwordForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  document.getElementById("successBox").hidden = true;
  document.getElementById("errorBox").hidden = true;
  clearAllFieldErrors(document.getElementById("passwordForm"));

  const vecchiaPassword = document.getElementById("fVecchia").value;
  const nuovaPassword = document.getElementById("fNuova").value;
  const conferma = document.getElementById("fConferma").value;

  if (nuovaPassword.length < 8) {
    setFieldError("fNuova", "La nuova password deve avere almeno 8 caratteri.");
    return;
  }
  if (nuovaPassword !== conferma) {
    setFieldError("fConferma", "Le due password non coincidono.");
    return;
  }

  try {
    await api.put("/api/auth/password", { vecchiaPassword, nuovaPassword });
    document.getElementById("passwordForm").reset();
    const box = document.getElementById("successBox");
    box.textContent = "Password aggiornata con successo.";
    box.hidden = false;
  } catch (err) {
    showError(err.message);
  }
});
