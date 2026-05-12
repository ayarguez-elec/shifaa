/* =========================================
   SHIFAA - EventCard Component
   src/components/EventCard.js
   ========================================= */

/**
 * Returns an SVG illustration based on event type.
 * These act as "thematic images" for each event type.
 */
function getEventIllustration(type) {
  const illustrations = {

    "Conférence": `
      <svg viewBox="0 0 400 160" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:100%">
        <defs>
          <linearGradient id="conf-bg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#1e3a5f"/>
            <stop offset="100%" style="stop-color:#2563eb"/>
          </linearGradient>
        </defs>
        <!-- Background -->
        <rect width="400" height="160" fill="url(#conf-bg)"/>
        <!-- Decorative circles -->
        <circle cx="350" cy="20"  r="60" fill="rgba(255,255,255,0.04)"/>
        <circle cx="30"  cy="140" r="50" fill="rgba(255,255,255,0.04)"/>
        <!-- Stage / podium -->
        <rect x="150" y="90" width="100" height="8"  rx="4" fill="rgba(255,255,255,0.25)"/>
        <rect x="165" y="98" width="70"  height="30" rx="3" fill="rgba(255,255,255,0.12)"/>
        <!-- Podium speaker figure -->
        <circle cx="200" cy="68" r="14" fill="rgba(255,255,255,0.30)"/>
        <rect   x="190" y="82" width="20" height="10" rx="3" fill="rgba(255,255,255,0.20)"/>
        <!-- Audience seats -->
        <rect x="50"  y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="75"  y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="100" y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="50"  y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <rect x="75"  y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <rect x="100" y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <rect x="264" y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="289" y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="314" y="108" width="18" height="12" rx="3" fill="rgba(255,255,255,0.15)"/>
        <rect x="264" y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <rect x="289" y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <rect x="314" y="124" width="18" height="12" rx="3" fill="rgba(255,255,255,0.10)"/>
        <!-- Mic icon -->
        <rect x="196" y="85" width="8" height="14" rx="4" fill="rgba(255,255,255,0.6)"/>
        <path d="M192 96 Q200 104 208 96" stroke="rgba(255,255,255,0.6)" stroke-width="1.5" fill="none"/>
        <!-- Title text -->
        <text x="20" y="35" font-family="Poppins,sans-serif" font-size="14" font-weight="700" fill="white" opacity="0.9">CONFÉRENCE</text>
        <text x="20" y="52" font-family="sans-serif" font-size="9" fill="rgba(255,255,255,0.55)">Médecine · Spécialistes · Innovations</text>
        <!-- Stars -->
        <circle cx="370" cy="140" r="2" fill="rgba(255,255,255,0.4)"/>
        <circle cx="380" cy="120" r="1.5" fill="rgba(255,255,255,0.3)"/>
        <circle cx="360" cy="115" r="1"   fill="rgba(255,255,255,0.25)"/>
      </svg>`,

    "Formation": `
      <svg viewBox="0 0 400 160" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:100%">
        <defs>
          <linearGradient id="form-bg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#78350f"/>
            <stop offset="100%" style="stop-color:#d97706"/>
          </linearGradient>
        </defs>
        <rect width="400" height="160" fill="url(#form-bg)"/>
        <circle cx="380" cy="0"   r="80" fill="rgba(255,255,255,0.05)"/>
        <circle cx="20"  cy="160" r="60" fill="rgba(255,255,255,0.05)"/>
        <!-- Stethoscope drawing -->
        <circle cx="290" cy="80" r="32" fill="rgba(255,255,255,0.08)" stroke="rgba(255,255,255,0.20)" stroke-width="2.5"/>
        <circle cx="290" cy="80" r="18" fill="rgba(255,255,255,0.12)"/>
        <!-- Tube -->
        <path d="M258 80 Q240 60 250 40 Q260 20 280 30" stroke="rgba(255,255,255,0.35)" stroke-width="4" fill="none" stroke-linecap="round"/>
        <circle cx="282" cy="27" r="6" fill="rgba(255,255,255,0.4)" stroke="rgba(255,255,255,0.6)" stroke-width="1.5"/>
        <!-- Heartbeat line -->
        <polyline points="40,90 70,90 85,55 100,120 115,70 130,90 170,90" stroke="rgba(255,255,255,0.5)" stroke-width="2.5" fill="none" stroke-linejoin="round"/>
        <!-- Notebook icon -->
        <rect x="30" y="100" width="30" height="40" rx="3" fill="rgba(255,255,255,0.15)"/>
        <line x1="35" y1="108" x2="55" y2="108" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
        <line x1="35" y1="114" x2="55" y2="114" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
        <line x1="35" y1="120" x2="50" y2="120" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
        <!-- Red cross badge -->
        <rect x="350" y="110" width="36" height="36" rx="18" fill="rgba(255,255,255,0.15)"/>
        <rect x="362" y="118" width="12" height="4"  rx="2" fill="rgba(255,255,255,0.7)"/>
        <rect x="366" y="114" width="4"  height="12" rx="2" fill="rgba(255,255,255,0.7)"/>
        <!-- Labels -->
        <text x="20" y="35" font-family="Poppins,sans-serif" font-size="14" font-weight="700" fill="white" opacity="0.9">FORMATION</text>
        <text x="20" y="52" font-family="sans-serif" font-size="9" fill="rgba(255,255,255,0.55)">Pratique · Certification · Compétences</text>
      </svg>`,

    "Séminaire": `
      <svg viewBox="0 0 400 160" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:100%">
        <defs>
          <linearGradient id="sem-bg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#14532d"/>
            <stop offset="100%" style="stop-color:#16a34a"/>
          </linearGradient>
        </defs>
        <rect width="400" height="160" fill="url(#sem-bg)"/>
        <circle cx="360" cy="30"  r="70" fill="rgba(255,255,255,0.05)"/>
        <circle cx="40"  cy="150" r="55" fill="rgba(255,255,255,0.05)"/>
        <!-- Round table conference -->
        <ellipse cx="250" cy="100" rx="80" ry="40" fill="rgba(255,255,255,0.08)" stroke="rgba(255,255,255,0.15)" stroke-width="1.5"/>
        <!-- Chairs / people around table -->
        <circle cx="250" cy="58"  r="10" fill="rgba(255,255,255,0.22)"/>
        <circle cx="250" cy="142" r="10" fill="rgba(255,255,255,0.22)"/>
        <circle cx="175" cy="100" r="10" fill="rgba(255,255,255,0.22)"/>
        <circle cx="325" cy="100" r="10" fill="rgba(255,255,255,0.22)"/>
        <circle cx="190" cy="70"  r="10" fill="rgba(255,255,255,0.18)"/>
        <circle cx="310" cy="70"  r="10" fill="rgba(255,255,255,0.18)"/>
        <circle cx="190" cy="130" r="10" fill="rgba(255,255,255,0.18)"/>
        <circle cx="310" cy="130" r="10" fill="rgba(255,255,255,0.18)"/>
        <!-- Brain icon (mental health) -->
        <path d="M60 70 Q55 55 65 50 Q75 45 80 55 Q90 40 100 50 Q110 45 110 60 Q120 65 115 78 Q110 90 100 88 Q90 95 80 90 Q65 95 60 85 Q50 78 60 70Z"
              fill="rgba(255,255,255,0.18)" stroke="rgba(255,255,255,0.35)" stroke-width="1.5"/>
        <!-- Labels -->
        <text x="20" y="35" font-family="Poppins,sans-serif" font-size="14" font-weight="700" fill="white" opacity="0.9">SÉMINAIRE</text>
        <text x="20" y="52" font-family="sans-serif" font-size="9" fill="rgba(255,255,255,0.55)">Échanges · Expertise · Collaboration</text>
      </svg>`,

    "Atelier": `
      <svg viewBox="0 0 400 160" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:100%">
        <defs>
          <linearGradient id="atl-bg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#3b0764"/>
            <stop offset="100%" style="stop-color:#9333ea"/>
          </linearGradient>
        </defs>
        <rect width="400" height="160" fill="url(#atl-bg)"/>
        <circle cx="370" cy="10"  r="70" fill="rgba(255,255,255,0.05)"/>
        <circle cx="10"  cy="160" r="60" fill="rgba(255,255,255,0.05)"/>
        <!-- Food/nutrition plate -->
        <circle cx="290" cy="90" r="45" fill="rgba(255,255,255,0.06)" stroke="rgba(255,255,255,0.15)" stroke-width="2"/>
        <circle cx="290" cy="90" r="35" fill="rgba(255,255,255,0.08)"/>
        <!-- Pie chart segments (plate sections) -->
        <path d="M290 90 L290 55 A35 35 0 0 1 325 90 Z" fill="rgba(255,200,100,0.35)"/>
        <path d="M290 90 L325 90 A35 35 0 0 1 290 125 Z" fill="rgba(100,220,130,0.35)"/>
        <path d="M290 90 L290 125 A35 35 0 0 1 255 90 Z" fill="rgba(255,130,100,0.35)"/>
        <path d="M290 90 L255 90 A35 35 0 0 1 290 55 Z" fill="rgba(130,180,255,0.35)"/>
        <!-- Fork & knife -->
        <line x1="340" y1="60" x2="340" y2="120" stroke="rgba(255,255,255,0.4)" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="352" y1="60" x2="352" y2="120" stroke="rgba(255,255,255,0.4)" stroke-width="2.5" stroke-linecap="round"/>
        <path d="M340 60 Q346 72 352 60" stroke="rgba(255,255,255,0.4)" stroke-width="2" fill="none"/>
        <!-- Blood sugar drop icon -->
        <path d="M100 50 L88 75 Q88 90 100 90 Q112 90 112 75 Z" fill="rgba(255,255,255,0.25)" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
        <!-- Scale/balance icon -->
        <rect x="40" y="110" width="60" height="4" rx="2" fill="rgba(255,255,255,0.25)"/>
        <rect x="67" y="92"  width="6"  height="18" rx="3" fill="rgba(255,255,255,0.3)"/>
        <rect x="40" y="114" width="20" height="12" rx="3" fill="rgba(255,255,255,0.18)"/>
        <rect x="80" y="114" width="20" height="12" rx="3" fill="rgba(255,255,255,0.18)"/>
        <!-- Labels -->
        <text x="20" y="35" font-family="Poppins,sans-serif" font-size="14" font-weight="700" fill="white" opacity="0.9">ATELIER</text>
        <text x="20" y="52" font-family="sans-serif" font-size="9" fill="rgba(255,255,255,0.55)">Pratique · Interactif · Nutrition</text>
      </svg>`
  };

  return illustrations[type] || illustrations["Conférence"];
}

/**
 * Render one event card HTML string
 * @param {Object} event
 * @returns {string} HTML
 */
function renderEventCard(event) {
  const pct     = Math.round((event.inscrits / event.capacite) * 100);
  const ptCount = ShifaaDB.getParticipantsByEvent(event.id).length;

  const typeClass   = { "Conférence": "badge-conference", "Formation": "badge-formation", "Séminaire": "badge-seminaire", "Atelier": "badge-atelier" }[event.type] || "badge-conference";
  const statusClass = { "Actif": "ev-status-actif", "Terminé": "ev-status-termine", "Annulé": "ev-status-annule" }[event.statut] || "ev-status-actif";
  const statusIcon  = event.statut === "Actif" ? "✓" : event.statut === "Terminé" ? "◉" : "✕";

  // Format date (French)
  const dateObj = new Date(event.date + 'T00:00:00');
  const dateFR  = dateObj.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });

  // Progress bar color
  const barColor = pct >= 90 ? '#e05a5a' : pct >= 70 ? '#e8a020' : '#27ae60';

  return `
    <div class="event-card" data-type="${event.type}" data-id="${event.id}">

      <!-- Thematic illustration banner -->
      <div class="event-card-image-wrapper">
        ${getEventIllustration(event.type)}
        <div class="event-type-overlay">
          <span class="badge ${typeClass}">${event.type}</span>
        </div>
        <div class="event-status-overlay">
          <span class="${statusClass}">${statusIcon} ${event.statut}</span>
        </div>
      </div>

      <!-- Card body -->
      <div class="event-card-body">
        <div class="event-card-title">${event.titre}</div>

        <div class="event-meta">
          <span class="meta-chip">
            <svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z"/></svg>
            ${dateFR} · ${event.heure}
          </span>
          <span class="meta-chip">
            <svg viewBox="0 0 24 24"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>
            ${event.lieu}
          </span>
          <span class="meta-chip">
            <svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
            ${ptCount} participant(s)
          </span>
        </div>

        <!-- Capacity progress bar -->
        <div class="capacity-bar">
          <div class="capacity-label">
            <span>Capacité : ${event.inscrits} / ${event.capacite}</span>
            <span style="font-weight:600;color:${barColor}">${pct}%</span>
          </div>
          <div class="capacity-track">
            <div class="capacity-fill" style="width:${pct}%;background:linear-gradient(90deg,${barColor},${barColor}cc)"></div>
          </div>
        </div>

        <div class="event-card-desc">${event.description}</div>

        <!-- Organizer -->
        <div style="font-size:11.5px;color:var(--text-light);margin-bottom:12px">
          👤 <strong>${event.organisateur}</strong>
        </div>

        <!-- Actions -->
        <div class="event-card-actions">
          <button class="btn btn-dark btn-sm" onclick="EventsPage.openEditModal(${event.id})">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:13px;height:13px"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
            Modifier
          </button>
          <button class="btn btn-danger btn-sm" onclick="EventsPage.confirmDelete(${event.id},'${event.titre.replace(/'/g,"\\'")}')">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:13px;height:13px"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            Supprimer
          </button>
          <button class="btn btn-outline btn-sm" onclick="App.navigate('participants','ev${event.id}')">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:13px;height:13px"><path d="M16 11c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 3-1.34 3-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
            Participants
          </button>
        </div>
      </div>
    </div>
  `;
}
