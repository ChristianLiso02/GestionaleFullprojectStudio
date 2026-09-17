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
