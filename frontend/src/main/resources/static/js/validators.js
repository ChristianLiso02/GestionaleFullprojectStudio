// Validazioni condivise per i dati personali (codice fiscale, telefono, email).
// Ogni funzione ritorna null se il valore è valido, altrimenti un messaggio d'errore in italiano.

const CF_DISPARI = {
  "0": 1, "1": 0, "2": 5, "3": 7, "4": 9, "5": 13, "6": 15, "7": 17, "8": 19, "9": 21,
  A: 1, B: 0, C: 5, D: 7, E: 9, F: 13, G: 15, H: 17, I: 19, J: 21, K: 2, L: 4, M: 18,
  N: 20, O: 11, P: 3, Q: 6, R: 8, S: 12, T: 14, U: 16, V: 10, W: 22, X: 25, Y: 24, Z: 23
};
const CF_PARI = {
  "0": 0, "1": 1, "2": 2, "3": 3, "4": 4, "5": 5, "6": 6, "7": 7, "8": 8, "9": 9,
  A: 0, B: 1, C: 2, D: 3, E: 4, F: 5, G: 6, H: 7, I: 8, J: 9, K: 10, L: 11, M: 12,
  N: 13, O: 14, P: 15, Q: 16, R: 17, S: 18, T: 19, U: 20, V: 21, W: 22, X: 23, Y: 24, Z: 25
};
const CF_RESTO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

function validateCodiceFiscale(valore) {
  if (!valore) return null; // campo facoltativo: se vuoto, nessun errore
  const cf = valore.trim().toUpperCase();

  if (!/^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$/.test(cf)) {
    return "Formato non valido (es. RSSMRA80A01H501U).";
  }

  let somma = 0;
  for (let i = 0; i < 15; i++) {
    somma += (i % 2 === 0) ? CF_DISPARI[cf[i]] : CF_PARI[cf[i]];
  }
  const atteso = CF_RESTO[somma % 26];
  if (atteso !== cf[15]) {
    return "Codice fiscale non valido: il carattere di controllo non corrisponde.";
  }
  return null;
}

function validateTelefono(valore) {
  if (!valore) return null; // campo facoltativo
  const cifre = valore.replace(/[^0-9]/g, "");
  if (!/^[0-9+\s()-]+$/.test(valore.trim())) {
    return "Il telefono può contenere solo cifre, spazi e + ( ) -.";
  }
  if (cifre.length < 8 || cifre.length > 15) {
    return "Il numero di telefono deve avere tra 8 e 15 cifre.";
  }
  return null;
}

function validateEmail(valore) {
  if (!valore) return null; // campo facoltativo
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(valore.trim());
  return ok ? null : "Indirizzo email non valido.";
}
