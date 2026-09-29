// Icone (tratto, 24x24): disegnate a mano, senza librerie esterne.
const ICONE = {
  dashboard: '<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
  statistiche: '<path d="M4 20V10"/><path d="M10 20V4"/><path d="M16 20v-7"/><path d="M22 20H2"/>',
  studenti: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-7 8-7s8 3 8 7"/>',
  iscrizioni: '<path d="M9 15l6-6"/><path d="M11 6l1.5-1.5a4.2 4.2 0 0 1 6 6L17 12"/><path d="M13 18l-1.5 1.5a4.2 4.2 0 0 1-6-6L7 12"/>',
  pagamenti: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/><path d="M6 15h4"/>',
  quote: '<rect x="3" y="4" width="18" height="17" rx="2"/><path d="M3 9h18"/><path d="M8 2v4M16 2v4"/><path d="M8 14l2.5 2.5L16 13"/>',
  presenze: '<rect x="3" y="3" width="18" height="18" rx="2"/><path d="M8 12l3 3 5-6"/>',
  stagioni: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
  corsi: '<path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/>',
  istruttori: '<circle cx="12" cy="7" r="4"/><path d="M5 21v-1a7 7 0 0 1 14 0v1"/><path d="M12 14l1.2 2.4 2.6.4-1.9 1.8.5 2.6-2.4-1.3-2.4 1.3.5-2.6-1.9-1.8 2.6-.4z"/>',
  sale: '<path d="M3 21V6l9-4 9 4v15"/><path d="M9 21v-7h6v7"/>',
  abbonamenti: '<path d="M3 8a2 2 0 0 0 2-2h14a2 2 0 0 0 2 2v2a2 2 0 0 0 0 4v2a2 2 0 0 0-2 2H5a2 2 0 0 0-2-2v-2a2 2 0 0 0 0-4z"/><path d="M13 6v2M13 11v2M13 16v2"/>',
  backup: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M7 10l5 5 5-5"/><path d="M12 15V3"/>',
  profilo: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-7 8-7s8 3 8 7"/>',
  impostazioni: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>',
  esci: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>',
  freccia: '<path d="M6 9l6 6 6-6"/>',
  menu: '<path d="M3 6h18M3 12h18M3 18h18"/>'
};

function icona(nome, classe = "") {
  return `<svg class="${classe}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONE[nome] || ""}</svg>`;
}

const NAV_ITEMS = [
  { group: "Panoramica", items: [
    { view: "dashboard", href: "dashboard.html", label: "Dashboard" },
    { view: "statistiche", href: "statistiche.html", label: "Statistiche" }
  ]},
  { group: "Segreteria", items: [
    { view: "studenti", href: "studenti.html", label: "Studenti" },
    { view: "iscrizioni", href: "iscrizioni.html", label: "Iscrizioni" },
    { view: "pagamenti", href: "pagamenti.html", label: "Pagamenti" },
    { view: "quote", href: "quote.html", label: "Quote mensili" },
    { view: "presenze", href: "presenze.html", label: "Presenze" }
  ]},
  { group: "Organizzazione", items: [
    { view: "stagioni", href: "stagioni.html", label: "Stagioni" },
    { view: "corsi", href: "corsi.html", label: "Corsi" },
    { view: "istruttori", href: "istruttori.html", label: "Istruttori" },
    { view: "sale", href: "sale.html", label: "Sale" },
    { view: "abbonamenti", href: "abbonamenti.html", label: "Abbonamenti" }
  ]},
  { group: "Sistema", items: [
    { view: "backup", href: "backup.html", label: "Backup" }
  ]}
];

function renderNav(activeView, pageTitle, pageSub) {
  const user = getUser() || { nome: "", cognome: "", ruolo: "" };

  const navHtml = NAV_ITEMS.map(section => `
    <div class="nav-group-label">${section.group}</div>
    ${section.items.map(item => `
      <a class="nav-link${item.view === activeView ? " active" : ""}" href="${item.href}"${item.view === activeView ? ' aria-current="page"' : ""}>
        ${icona(item.view)}<span>${item.label}</span>
      </a>
    `).join("")}
  `).join("");

  const sidebarEl = document.getElementById("sidebar-container");
  if (sidebarEl) {
    sidebarEl.innerHTML = `
      <div class="sidebar" id="sidebar">
        <div class="brand">
          <div class="mark">FullProject<span>Studio</span></div>
          <div class="tag">Area segreteria</div>
        </div>
        <nav class="nav" aria-label="Menu principale">${navHtml}</nav>
        <div class="sidebar-foot">Gestionale interno &mdash; solo staff.</div>
      </div>
      <div class="sidebar-sfondo" id="sidebarSfondo"></div>
    `;
  }

  const topbarEl = document.getElementById("topbar-container");
  if (topbarEl) {
    const nomeCompleto = `${user.nome || ""} ${user.cognome || ""}`.trim();
    const initials = ((user.nome || "?")[0] + (user.cognome || "?")[0]).toUpperCase();
    const ruolo = user.ruolo === "ADMIN" ? "Amministratore" : user.ruolo === "SEGRETERIA" ? "Segreteria" : (user.ruolo || "");
    topbarEl.innerHTML = `
      <div class="topbar">
        <div class="topbar-titolo">
          <button type="button" class="btn-menu-mobile" id="btnMenuMobile" aria-label="Apri il menu" aria-controls="sidebar" aria-expanded="false">${icona("menu")}</button>
          <div>
            <h1>${pageTitle || ""}</h1>
            <div class="sub">${pageSub || ""}</div>
          </div>
        </div>
        <div class="menu-utente">
          <button type="button" class="menu-utente-btn" id="menuUtenteBtn" aria-haspopup="menu" aria-expanded="false" aria-controls="menuUtente">
            <div class="avatar">${escapeHtml(initials)}</div>
            <div class="who"><b>${escapeHtml(nomeCompleto)}</b><span>${escapeHtml(ruolo)}</span></div>
            ${icona("freccia", "freccia")}
          </button>
          <div class="menu-utente-tendina" id="menuUtente" role="menu" hidden>
            <div class="menu-intestazione"><b>${escapeHtml(nomeCompleto)}</b><span>${escapeHtml(user.username || "")} · ${escapeHtml(ruolo)}</span></div>
            <a class="menu-voce${activeView === "profilo" ? " attiva" : ""}" role="menuitem" href="profilo.html">${icona("profilo")}Profilo</a>
            <a class="menu-voce${activeView === "impostazioni" ? " attiva" : ""}" role="menuitem" href="impostazioni.html">${icona("impostazioni")}Impostazioni</a>
            <div class="menu-separatore" role="separator"></div>
            <button type="button" class="menu-voce esci" role="menuitem" onclick="logout()">${icona("esci")}Esci</button>
          </div>
        </div>
      </div>
    `;
    attivaMenuUtente();
    attivaMenuMobile();
  }
}

// Apre/chiude la tendina del nome: si chiude cliccando fuori, con Esc o scegliendo una voce.
function attivaMenuUtente() {
  const btn = document.getElementById("menuUtenteBtn");
  const menu = document.getElementById("menuUtente");
  const voci = () => [...menu.querySelectorAll('[role="menuitem"]')];
  const apri = (focusPrima) => {
    menu.hidden = false;
    btn.setAttribute("aria-expanded", "true");
    if (focusPrima) voci()[0].focus();
  };
  const chiudi = (restituisciFocus) => {
    menu.hidden = true;
    btn.setAttribute("aria-expanded", "false");
    if (restituisciFocus) btn.focus();
  };
  btn.addEventListener("click", () => (menu.hidden ? apri(false) : chiudi(false)));
  btn.addEventListener("keydown", e => {
    if (e.key === "ArrowDown") { e.preventDefault(); apri(true); }
  });
  menu.addEventListener("keydown", e => {
    const lista = voci();
    const i = lista.indexOf(document.activeElement);
    if (e.key === "Escape") { e.preventDefault(); chiudi(true); }
    if (e.key === "ArrowDown") { e.preventDefault(); lista[(i + 1) % lista.length].focus(); }
    if (e.key === "ArrowUp") { e.preventDefault(); lista[(i - 1 + lista.length) % lista.length].focus(); }
    if (e.key === "Tab") chiudi(false);
  });
  document.addEventListener("click", e => {
    if (!menu.hidden && !e.target.closest(".menu-utente")) chiudi(false);
  });
}

// Su schermi stretti la barra laterale è nascosta e si apre con il pulsante ☰.
function attivaMenuMobile() {
  const btn = document.getElementById("btnMenuMobile");
  const sidebar = document.getElementById("sidebar");
  const sfondo = document.getElementById("sidebarSfondo");
  if (!btn || !sidebar) return;
  const imposta = aperto => {
    sidebar.classList.toggle("open", aperto);
    sfondo.classList.toggle("visibile", aperto);
    btn.setAttribute("aria-expanded", String(aperto));
  };
  btn.addEventListener("click", () => imposta(!sidebar.classList.contains("open")));
  sfondo.addEventListener("click", () => imposta(false));
  document.addEventListener("keydown", e => { if (e.key === "Escape") imposta(false); });
}
