/* =========================================
   SHIFAA - Events Page Controller
   src/pages/EventsPage.js
   ========================================= */

const EventsPage = {

  // Current filter state
  filters: { search: '', type: '', statut: '' },

  /**
   * Mount this page into #evenements-root
   */
  mount() {
    const root = document.getElementById('evenements-root');
    root.innerHTML = this._buildLayout();
    this._attachListeners();
    this.render();
  },

  /**
   * Build the static page skeleton HTML
   */
  _buildLayout() {
    return `
      <!-- Page header -->
      <div class="page-header" style="padding:0 0 22px">
        <div class="page-header-inner">
          <div>
            <div class="page-title">
              <div class="page-title-icon">
                <svg viewBox="0 0 24 24"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>
              </div>
              Gestion des Événements
            </div>
            <p class="page-subtitle">Gérez et suivez tous les événements médicaux</p>
          </div>
          <button class="btn btn-primary btn-lg" onclick="EventsPage.openAddModal()">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:18px;height:18px"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            Ajouter un événement
          </button>
        </div>
      </div>

      <!-- Stats chips -->
      <div class="stats-bar" id="ev-stats-bar"></div>

      <!-- Filters bar -->
      <div class="filters-bar">
        <div class="search-box">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <input type="text" id="ev-search" placeholder="Rechercher un événement..." oninput="EventsPage.onFilter()">
        </div>

        <select class="filter-select" id="ev-type-filter" onchange="EventsPage.onFilter()">
          <option value="">Type : Tous</option>
          <option value="Conférence">Conférence</option>
          <option value="Formation">Formation</option>
          <option value="Séminaire">Séminaire</option>
          <option value="Atelier">Atelier</option>
        </select>

        <select class="filter-select" id="ev-statut-filter" onchange="EventsPage.onFilter()">
          <option value="">Statut : Tous</option>
          <option value="Actif">Actif</option>
          <option value="Terminé">Terminé</option>
          <option value="Annulé">Annulé</option>
        </select>

        <div class="actions">
          <button class="btn btn-gray btn-sm" onclick="EventsPage.resetFilters()">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width:14px;height:14px"><path d="M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"/></svg>
            Réinitialiser
          </button>
        </div>
      </div>

      <!-- Total count -->
      <p id="ev-total-label" style="font-size:13px;color:var(--text-light);margin-bottom:16px"></p>

      <!-- Cards grid -->
      <div class="cards-grid" id="ev-grid"></div>
    `;
  },

  /**
   * Attach filter listeners (called once on mount)
   */
  _attachListeners() {
    // listeners already attached via inline oninput/onchange
  },

  /**
   * Filter handler
   */
  onFilter() {
    this.filters.search = document.getElementById('ev-search')?.value.toLowerCase() || '';
    this.filters.type   = document.getElementById('ev-type-filter')?.value || '';
    this.filters.statut = document.getElementById('ev-statut-filter')?.value || '';
    this.render();
  },

  /**
   * Reset all filters
   */
  resetFilters() {
    this.filters = { search: '', type: '', statut: '' };
    const s = document.getElementById('ev-search');
    const t = document.getElementById('ev-type-filter');
    const st = document.getElementById('ev-statut-filter');
    if (s) s.value = '';
    if (t) t.value = '';
    if (st) st.value = '';
    this.render();
  },

  /**
   * Apply filters and render cards
   */
  render() {
    const { search, type, statut } = this.filters;
    const filtered = ShifaaDB.events.filter(ev =>
      (!search || ev.titre.toLowerCase().includes(search) || ev.lieu.toLowerCase().includes(search) || ev.organisateur.toLowerCase().includes(search)) &&
      (!type   || ev.type === type) &&
      (!statut || ev.statut === statut)
    );

    // Stats bar
    const totalEv  = ShifaaDB.events.length;
    const actifs   = ShifaaDB.events.filter(e => e.statut === 'Actif').length;
    const termines = ShifaaDB.events.filter(e => e.statut === 'Terminé').length;
    const totalPt  = ShifaaDB.participants.length;

    const statsBar = document.getElementById('ev-stats-bar');
    if (statsBar) {
      statsBar.innerHTML = `
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#dbeafe">📅</div>
          <div><div class="stat-chip-label">Total événements</div><div class="stat-chip-value">${totalEv}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#d4edda">✅</div>
          <div><div class="stat-chip-label">Actifs</div><div class="stat-chip-value">${actifs}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#e2e8f0">◉</div>
          <div><div class="stat-chip-label">Terminés</div><div class="stat-chip-value">${termines}</div></div>
        </div>
        <div class="stat-chip">
          <div class="stat-chip-icon" style="background:#f3e8ff">👥</div>
          <div><div class="stat-chip-label">Participants</div><div class="stat-chip-value">${totalPt}</div></div>
        </div>
      `;
    }

    // Total label
    const label = document.getElementById('ev-total-label');
    if (label) label.textContent = `Total : ${filtered.length} événement(s) trouvé(s)`;

    // Grid
    const grid = document.getElementById('ev-grid');
    if (!grid) return;

    if (filtered.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column:1/-1">
          <span class="empty-state-icon">📭</span>
          <p>Aucun événement trouvé pour ces critères.</p>
        </div>`;
      return;
    }

    grid.innerHTML = filtered.map(ev => renderEventCard(ev)).join('');
  },

  // ==============================
  // CRUD - Modal Forms
  // ==============================

  /**
   * Build the event form modal HTML
   */
  _buildFormModal(event = null) {
    const isEdit  = !!event;
    const title   = isEdit ? 'Modifier l\'événement' : 'Ajouter un événement';
    const iconSvg = `<path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/>`;

    const v = (field, fallback = '') => event ? (event[field] ?? fallback) : fallback;
    const sel = (field, value) => v(field) === value ? 'selected' : '';

    return `
      ${Modal.buildHeader(title, iconSvg)}
      <div class="modal-body">
        <div class="form-row">
          <div class="form-group" style="grid-column:1/-1">
            <label class="form-label">Titre *</label>
            <input class="form-control" id="fm-titre" placeholder="Titre de l'événement" value="${v('titre')}">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Type *</label>
            <select class="form-control" id="fm-type">
              <option ${sel('type','Conférence')}>Conférence</option>
              <option ${sel('type','Formation')}>Formation</option>
              <option ${sel('type','Séminaire')}>Séminaire</option>
              <option ${sel('type','Atelier')}>Atelier</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Statut</label>
            <select class="form-control" id="fm-statut">
              <option ${sel('statut','Actif')}>Actif</option>
              <option ${sel('statut','Terminé')}>Terminé</option>
              <option ${sel('statut','Annulé')}>Annulé</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Date *</label>
            <input class="form-control" id="fm-date" type="date" value="${v('date')}">
          </div>
          <div class="form-group">
            <label class="form-label">Heure</label>
            <input class="form-control" id="fm-heure" type="time" value="${v('heure','09:00')}">
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Lieu *</label>
          <input class="form-control" id="fm-lieu" placeholder="Ex: Hôpital La Rabta, Tunis" value="${v('lieu')}">
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Capacité maximale</label>
            <input class="form-control" id="fm-capacite" type="number" min="1" placeholder="50" value="${v('capacite',50)}">
          </div>
          <div class="form-group">
            <label class="form-label">Organisateur</label>
            <input class="form-control" id="fm-orga" placeholder="Nom de l'organisateur" value="${v('organisateur')}">
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Description</label>
          <textarea class="form-control" id="fm-desc" placeholder="Description de l'événement...">${v('description')}</textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" onclick="Modal.close()">Annuler</button>
        <button class="btn-save" onclick="EventsPage.saveEvent(${isEdit ? event.id : 'null'})">
          ${isEdit ? 'Enregistrer les modifications' : 'Ajouter l\'événement'}
        </button>
      </div>
    `;
  },

  openAddModal() {
    Modal.open(this._buildFormModal(), 'md');
  },

  openEditModal(id) {
    const ev = ShifaaDB.getEvent(id);
    if (!ev) return;
    Modal.open(this._buildFormModal(ev), 'md');
  },

  saveEvent(id) {
    // Validate
    const titre = document.getElementById('fm-titre')?.value.trim();
    const lieu  = document.getElementById('fm-lieu')?.value.trim();
    const date  = document.getElementById('fm-date')?.value;
    if (!titre) { Toast.warning('Le titre est obligatoire.'); return; }
    if (!lieu)  { Toast.warning('Le lieu est obligatoire.'); return; }
    if (!date)  { Toast.warning('La date est obligatoire.'); return; }

    const data = {
      titre,
      lieu,
      date,
      type:         document.getElementById('fm-type')?.value,
      statut:       document.getElementById('fm-statut')?.value,
      heure:        document.getElementById('fm-heure')?.value || '09:00',
      capacite:     parseInt(document.getElementById('fm-capacite')?.value) || 50,
      organisateur: document.getElementById('fm-orga')?.value.trim() || '—',
      description:  document.getElementById('fm-desc')?.value.trim() || '',
    };

    if (id) {
      ShifaaDB.updateEvent(id, data);
      Toast.success('Événement modifié avec succès !');
    } else {
      ShifaaDB.addEvent(data);
      Toast.success('Événement ajouté avec succès !');
    }

    Modal.close();
    this.render();
  },

  confirmDelete(id, name) {
    Modal.confirm(
      `Voulez-vous vraiment supprimer l'événement <strong>"${name}"</strong> ?<br><small>Tous les participants liés seront également supprimés.</small>`,
      () => {
        ShifaaDB.deleteEvent(id);
        Toast.success('Événement supprimé.');
        this.render();
      }
    );
  }
};
