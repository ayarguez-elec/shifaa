/* =========================================
   SHIFAA - ParticipantCard Component
   src/components/ParticipantCard.js
   ========================================= */

/**
 * Get initials from first + last name
 */
function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

/**
 * Get avatar CSS class based on role
 */
function getAvatarClass(role) {
  return {
    "Docteur":    "avatar-docteur",
    "Patient":    "avatar-patient",
    "Pharmacien": "avatar-pharma",
    "Assistant":  "avatar-assistant"
  }[role] || "avatar-default";
}

/**
 * Get role badge class
 */
function getRoleBadgeClass(role) {
  return {
    "Docteur":    "badge badge-docteur",
    "Patient":    "badge badge-patient",
    "Pharmacien": "badge badge-pharma",
    "Assistant":  "badge badge-assistant"
  }[role] || "badge badge-patient";
}

/**
 * Render one participant card HTML string
 * @param {Object} pt - participant object
 * @returns {string} HTML
 */
function renderParticipantCard(pt) {
  const initials    = getInitials(pt.prenom, pt.nom);
  const avatarClass = getAvatarClass(pt.role);
  const roleBadge   = getRoleBadgeClass(pt.role);
  const event       = ShifaaDB.getEvent(pt.eventId);
  const eventTitle  = event ? event.titre : '— Aucun événement —';

  const statusBadge = pt.statut === "Actif"
    ? `<span class="badge badge-actif">✓ Actif</span>`
    : `<span class="badge badge-suspendu">⊘ Suspendu</span>`;

  return `
    <div class="participant-card" data-id="${pt.id}">

      <!-- Header: avatar + name + role -->
      <div class="participant-card-header">
        <div class="participant-avatar ${avatarClass}">${initials}</div>
        <div class="participant-card-name-area">
          <div class="participant-card-name">${pt.prenom} ${pt.nom}</div>
          <span class="${roleBadge}">${pt.role}</span>
        </div>
      </div>

      <!-- Info rows -->
      <div class="participant-info-row">
        <svg viewBox="0 0 24 24"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>
        <span>${pt.email}</span>
      </div>

      <div class="participant-info-row">
        <svg viewBox="0 0 24 24"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>
        <span>${pt.tel || 'Non renseigné'}</span>
      </div>

      ${pt.age ? `
      <div class="participant-info-row">
        <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
        <span>${pt.age} ans · ${pt.adresse || ''}</span>
      </div>` : ''}

      <!-- Status -->
      <div class="participant-status-row">${statusBadge}</div>

      <!-- Event chip -->
      <div class="participant-event-chip">
        <svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>
        ${eventTitle}
      </div>

      <!-- Actions -->
      <div class="participant-card-actions">
        <button class="btn btn-dark btn-sm" onclick="ParticipantsPage.openEditModal(${pt.id})">
          <svg viewBox="0 0 24 24" fill="currentColor" style="width:13px;height:13px"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
          Modifier
        </button>
        <button class="btn btn-danger btn-sm" onclick="ParticipantsPage.confirmDelete(${pt.id},'${(pt.prenom + ' ' + pt.nom).replace(/'/g,"\\'")}')">
          <svg viewBox="0 0 24 24" fill="currentColor" style="width:13px;height:13px"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z"/></svg>
          Supprimer
        </button>
      </div>
    </div>
  `;
}
