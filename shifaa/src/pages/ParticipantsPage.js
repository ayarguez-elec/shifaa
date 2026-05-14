/* =========================================
   SHIFAA - Participants Page Controller
   src/pages/ParticipantsPage.js
   ========================================= */

const ParticipantsPage = {

  filters: { search: '', role: '', statut: '', eventId: null },

  /**
   * Mount this page into #participants-root
   * @param {string|null} preset  - e.g. 'ev1' to pre-filter by event 1
   */
  mount(preset = null) {
    const root = document.getElementById('participants-root');
    root.innerHTML = this._buildLayout();
    this._attachListeners();

    // Pre-filter by event if coming from events page
    if (preset && preset.startsWith('ev')) {
      const evId = parseInt(preset.replace('ev', ''));
      this.filters.eventId = evId;
      const sel = document.getElementById('pt-event-filter');
      if (sel) sel.value = evId;
    } else {
      this.filters.eventId = null;
    }

    this.render();
  },

  _buildLayout() {
    // Build event options for filter dropdown
    const eventOptions = ShifaaDB.events.map(ev =>
      `<option value="${ev.id}">${ev.titre}</option>`
    ).join('');

    return `
      <!-- Page header -->
      <div class="page-header" style="padding:0 0 22px">
        <div class="page-header-inner">
          <div>
            <div class="page-title">
              <div class="page-title-icon">
                <svg viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
              </div>
              Gestion des Participants
            </div>
            <p class="page-subtitle">Gérez les inscriptions aux événements médicaux</p>
          </div>
          <button class="btn btn-primary btn-lg" onclick="ParticipantsPage.openAddModal()">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:18px;height:18px"><path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
            Ajouter un participant
          </button>
        </div>
      </div>

      <!-- Stats bar -->
      <div class="stats-bar" id="pt-stats-bar"></div>

      <!-- Filters bar -->
      <div class="filters-bar">
        <div class="search-box">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <input type="text" id="pt-search" placeholder="Rechercher par nom, email..." oninput="ParticipantsPage.onFilter()">
        </div>

        <select class="filter-select" id="pt-role-filter" onchange="ParticipantsPage.onFilter()">
          <option value="">Rôle : Tous</option>
          <option value="Docteur">Docteur</option>
          <option value="Patient">Patient</option>
          <option value="Pharmacien">Pharmacien</option>
          <option value="Assistant">Assistant</option>
        </select>

        <select class="filter-select" id="pt-statut-filter" onchange="ParticipantsPage.onFilter()">
          <option value="">Statut : Tous</option>
          <option value="Actif">Actif</option>
          <option value="Suspendu">Suspendu</option>
        </select>

        <select class="filter-select" id="pt-event-filter" onchange="ParticipantsPage.onFilter()">
          <option value="">Événement : Tous</option>
          ${eventOptions}
        </select>

        <div class="actions">
          <button class="btn btn-gray btn-sm" onclick="ParticipantsPage.resetFilters()">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:14px;height:14px"><path d="M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"/></svg>
            Réinitialiser
          </button>
        </div>
      </div>

      <!-- Count -->
      <p id="pt-total-label" style="font-size:13px;color:var(--text-light);margin-bottom:16px"></p>

      <!-- Grid -->
      <div class="cards-grid" id="pt-grid"></div>
    `;
  },

  _attachListeners() { /* inline oninput/onchange handle this */ },

  onFilter() {
    this.filters.search  = document.getElementById('pt-search')?.value.toLowerCase() || '';
    this.filters.role    = document.getElementById('pt-role-filter')?.value || '';
    this.filters.statut  = document.getElementById('pt-statut-filter')?.value || '';
    const evVal          = document.getElementById('pt-event-filter')?.value || '';
    this.filters.eventId = evVal ? parseInt(evVal) : null;
    this.render();
  },

  resetFilters() {
    this.filters = { search: '', role: '', statut: '', eventId: null };
    ['pt-search','pt-role-filter','pt-statut-filter','pt-event-filter'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
    this.render();
  },

  render() {
    const { search, role, statut, eventId } = this.filters;
    const filtered = ShifaaDB.participants.filter(pt =>
      (!search  || (pt.prenom + ' ' + pt.nom).toLowerCase().includes(search) || pt.email.toLowerCase().includes(search)) &&
      (!role    || pt.role === role) &&
      (!statut  || pt.statut === statut) &&
      (!eventId || pt.eventId === eventId)
    );

    // Stats bar
    const total   = ShifaaDB.participants.length;
    const actifs  = ShifaaDB.participants.filter(p => p.statut === 'Actif').length;
    const doctors = ShifaaDB.participants.filter(p => p.role === 'Docteur').length;
    const patients= ShifaaDB.participants.filter(p => p.role === 'Patient').length;

    const statsBar = document.getElementById('pt-stats-bar');
    if (statsBar) {
      statsBar.innerHTML = `
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#e8daff">👥</div>
          <div><div class="stat-chip-label">Total participants</div><div class="stat-chip-value">${total}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#d4edda">✅</div>
          <div><div class="stat-chip-label">Actifs</div><div class="stat-chip-value">${actifs}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#d4edda">🩺</div>
          <div><div class="stat-chip-label">Docteurs</div><div class="stat-chip-value">${doctors}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#cce5ff">🏥</div>
          <div><div class="stat-chip-label">Patients</div><div class="stat-chip-value">${patients}</div></div>
        </div>
      `;
    }

    // Total label
    const label = document.getElementById('pt-total-label');
    if (label) label.textContent = `Total : ${filtered.length} participant(s) trouvé(s)`;

    // Grid
    const grid = document.getElementById('pt-grid');
    if (!grid) return;

    if (filtered.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column:1/-1">
          <span class="empty-state-icon">👤</span>
          <p>Aucun participant trouvé pour ces critères.</p>
        </div>`;
      return;
    }

    grid.innerHTML = filtered.map(pt => renderParticipantCard(pt)).join('');
  },

  // ==============================
  // CRUD Forms
  // ==============================

  _buildFormModal(participant = null) {
    const isEdit  = !!participant;
    const title   = isEdit ? 'Modifier le participant' : 'Ajouter un participant';
    const iconSvg = `<path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>`;

    const v   = (field, fallback = '') => participant ? (participant[field] ?? fallback) : fallback;
    const sel = (field, value) => v(field) === value ? 'selected' : '';

    const eventOptions = ShifaaDB.events.map(ev =>
      `<option value="${ev.id}" ${participant && participant.eventId === ev.id ? 'selected' : ''}>${ev.titre}</option>`
    ).join('');

    return `
      ${Modal.buildHeader(title, iconSvg)}
      <div class="modal-body">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Prénom *</label>
            <input class="form-control" id="pt-prenom" placeholder="Prénom" value="${v('prenom')}">
          </div>
          <div class="form-group">
            <label class="form-label">Nom *</label>
            <input class="form-control" id="pt-nom" placeholder="Nom" value="${v('nom')}">
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Email *</label>
          <input class="form-control" id="pt-email" type="email" placeholder="email@exemple.com" value="${v('email')}">
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Téléphone</label>
            <input class="form-control" id="pt-tel" placeholder="+216 XX XXX XXX" value="${v('tel')}">
          </div>
          <div class="form-group">
            <label class="form-label">Âge</label>
            <input class="form-control" id="pt-age" type="number" min="1" max="120" placeholder="Ex: 35" value="${v('age')}">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Rôle</label>
            <select class="form-control" id="pt-role">
              <option ${sel('role','Patient')}>Patient</option>
              <option ${sel('role','Docteur')}>Docteur</option>
              <option ${sel('role','Pharmacien')}>Pharmacien</option>
              <option ${sel('role','Assistant')}>Assistant</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Statut</label>
            <select class="form-control" id="pt-statut">
              <option ${sel('statut','Actif')}>Actif</option>
              <option ${sel('statut','Suspendu')}>Suspendu</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Adresse</label>
          <input class="form-control" id="pt-adresse" placeholder="Ville ou adresse" value="${v('adresse')}">
        </div>

        <div class="form-group">
          <label class="form-label">Événement associé</label>
          <select class="form-control" id="pt-event-sel">
            ${eventOptions}
          </select>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" onclick="Modal.close()">Annuler</button>
        <button class="btn-save" onclick="ParticipantsPage.saveParticipant(${isEdit ? participant.id : 'null'})">
          ${isEdit ? 'Enregistrer les modifications' : 'Ajouter le participant'}
        </button>
      </div>
    `;
  },

  openAddModal() {
    Modal.open(this._buildFormModal(), 'md');
  },

  openEditModal(id) {
    const pt = ShifaaDB.getParticipant(id);
    if (!pt) return;
    Modal.open(this._buildFormModal(pt), 'md');
  },

  saveParticipant(id) {
    const prenom = document.getElementById('pt-prenom')?.value.trim();
    const nom    = document.getElementById('pt-nom')?.value.trim();
    const email  = document.getElementById('pt-email')?.value.trim();
    if (!prenom) { Toast.warning('Le prénom est obligatoire.'); return; }
    if (!nom)    { Toast.warning('Le nom est obligatoire.'); return; }
    if (!email)  { Toast.warning('L\'email est obligatoire.'); return; }

    const data = {
      prenom, nom, email,
      tel:     document.getElementById('pt-tel')?.value.trim() || '',
      age:     parseInt(document.getElementById('pt-age')?.value) || null,
      role:    document.getElementById('pt-role')?.value,
      statut:  document.getElementById('pt-statut')?.value,
      adresse: document.getElementById('pt-adresse')?.value.trim() || '',
      eventId: parseInt(document.getElementById('pt-event-sel')?.value) || ShifaaDB.events[0]?.id,
    };

    if (id) {
      ShifaaDB.updateParticipant(id, data);
      Toast.success('Participant modifié avec succès !');
    } else {
      ShifaaDB.addParticipant(data);
      Toast.success('Participant ajouté avec succès !');
    }

    Modal.close();
    this.render();
  },

  confirmDelete(id, name) {
    Modal.confirm(
      `Voulez-vous vraiment supprimer le participant <strong>"${name}"</strong> ?`,
      () => {
        ShifaaDB.deleteParticipant(id);
        Toast.success('Participant supprimé.');
        this.render();
      }
    );
  }
};
