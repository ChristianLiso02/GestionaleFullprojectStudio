// Wrapper fetch() verso le API REST del backend: aggiunge il token JWT,
// gestisce JSON ed errori in modo uniforme per tutte le pagine.
async function apiCall(path, { method = "GET", body } = {}) {
  const headers = { "Accept": "application/json" };
  const token = getToken();
  if (token) headers["Authorization"] = "Bearer " + token;
  if (body !== undefined) headers["Content-Type"] = "application/json";

  const response = await fetch(API_BASE_URL + path, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  if (response.status === 401) {
    clearSession();
    window.location.href = "login.html";
    throw new Error("Sessione scaduta");
  }

  if (response.status === 204) return null;

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = (data && data.message) ? data.message : "Errore " + response.status;
    throw new Error(message);
  }

  return data;
}

const api = {
  get: (path) => apiCall(path),
  post: (path, body) => apiCall(path, { method: "POST", body }),
  put: (path, body) => apiCall(path, { method: "PUT", body }),
  del: (path) => apiCall(path, { method: "DELETE" })
};

// Scarica un file dal backend (serve il token, quindi non basta un semplice link).
async function scaricaFile(path, nomeDiRiserva) {
  const response = await fetch(API_BASE_URL + path, { headers: { "Authorization": "Bearer " + getToken() } });
  if (response.status === 401) {
    clearSession();
    window.location.href = "login.html";
    return;
  }
  if (!response.ok) {
    const testo = await response.text();
    let messaggio = "Errore " + response.status;
    try { messaggio = JSON.parse(testo).message || messaggio; } catch (e) { /* risposta non JSON */ }
    throw new Error(messaggio);
  }
  const disposizione = response.headers.get("Content-Disposition") || "";
  const utf8 = disposizione.match(/filename\*=UTF-8''([^;]+)/i);
  const semplice = disposizione.match(/filename="([^"]+)"/i);
  const nome = utf8 ? decodeURIComponent(utf8[1]) : semplice ? semplice[1] : nomeDiRiserva;

  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement("a");
  link.href = url;
  link.download = nome;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
