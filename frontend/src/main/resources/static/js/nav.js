const NAV_ITEMS = [
  { group: "Panoramica", items: [
    { view: "dashboard", href: "dashboard.html", label: "Dashboard" }
  ]},
  { group: "Segreteria", items: [
    { view: "studenti", href: "studenti.html", label: "Studenti" },
    { view: "iscrizioni", href: "iscrizioni.html", label: "Iscrizioni" },
    { view: "pagamenti", href: "pagamenti.html", label: "Pagamenti" },
    { view: "presenze", href: "presenze.html", label: "Presenze" }
  ]},
  { group: "Organizzazione", items: [
    { view: "stagioni", href: "stagioni.html", label: "Stagioni" },
    { view: "corsi", href: "corsi.html", label: "Corsi" },
    { view: "istruttori", href: "istruttori.html", label: "Istruttori" },
    { view: "sale", href: "sale.html", label: "Sale" },
    { view: "abbonamenti", href: "abbonamenti.html", label: "Abbonamenti" }
  ]}
];

function renderNav(activeView, pageTitle, pageSub) {
  const user = getUser() || { nome: "", cognome: "", ruolo: "" };

  const navHtml = NAV_ITEMS.map(section => `
    <div class="nav-group-label">${section.group}</div>
    ${section.items.map(item => `
      <a class="nav-link${item.view === activeView ? " active" : ""}" href="${item.href}">${item.label}</a>
    `).join("")}
  `).join("");

  const sidebarEl = document.getElementById("sidebar-container");
  if (sidebarEl) {
    sidebarEl.innerHTML = `
      <div class="sidebar">
        <div class="brand">
          <div class="mark">FullProject<span>Studio</span></div>
          <div class="tag">Area segreteria</div>
        </div>
        <nav class="nav">${navHtml}</nav>
        <div class="sidebar-foot">Gestionale interno &mdash; solo staff.</div>
      </div>
    `;
  }

  const topbarEl = document.getElementById("topbar-container");
  if (topbarEl) {
    const initials = ((user.nome || "?")[0] + (user.cognome || "?")[0]).toUpperCase();
    topbarEl.innerHTML = `
      <div class="topbar">
        <div>
          <h1>${pageTitle || ""}</h1>
          <div class="sub">${pageSub || ""}</div>
        </div>
        <div class="user-chip">
          <div class="avatar">${initials}</div>
          <div class="who"><b>${user.nome || ""} ${user.cognome || ""}</b><span>ruolo ${user.ruolo || ""}</span></div>
          <a class="btn btn-ghost btn-sm" href="cambia-password.html">Cambia password</a>
          <button class="btn btn-ghost btn-sm" onclick="logout()">Esci</button>
        </div>
      </div>
    `;
  }
}
