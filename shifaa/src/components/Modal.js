/* =========================================
   SHIFAA - Modal Component
   src/components/Modal.js
   ========================================= */

const Modal = {

  _overlay: null,

  /**
   * Open a modal with given HTML content
   * @param {string} contentHTML
   * @param {'sm'|'md'|'lg'} size
   */
  open(contentHTML, size = 'md') {
    this.close(); // close any existing modal first

    const maxWidths = { sm: '380px', md: '560px', lg: '720px' };

    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'modal-overlay-main';
    overlay.innerHTML = `
      <div class="modal-box" style="max-width:${maxWidths[size] || maxWidths.md}">
        ${contentHTML}
      </div>
    `;

    // Close on overlay click
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) this.close();
    });

    document.getElementById('modals-container').appendChild(overlay);
    this._overlay = overlay;

    // Trigger open animation
    requestAnimationFrame(() => overlay.classList.add('open'));
  },

  /**
   * Close current modal
   */
  close() {
    if (this._overlay) {
      this._overlay.classList.remove('open');
      setTimeout(() => {
        if (this._overlay) {
          this._overlay.remove();
          this._overlay = null;
        }
      }, 220);
    }
  },

  /**
   * Open a confirmation dialog
   * @param {string} message   - e.g. 'Voulez-vous supprimer "John Doe" ?'
   * @param {Function} onConfirm
   */
  confirm(message, onConfirm) {
    const html = `
      <div class="modal-header">
        <div class="modal-header-left">
          <div class="modal-header-icon">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12 1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59 14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z"/></svg>
          </div>
          <span class="modal-title">Confirmer la suppression</span>
        </div>
        <button class="modal-close-btn" onclick="Modal.close()">✕</button>
      </div>
      <div class="confirm-body">
        <div class="confirm-icon-wrap">
          <svg viewBox="0 0 24 24"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
        </div>
        <div class="confirm-msg">${message}</div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" onclick="Modal.close()">Annuler</button>
        <button class="btn-save" style="background:#e05a5a" id="confirm-yes-btn">Supprimer</button>
      </div>
    `;
    this.open(html, 'sm');

    // Attach confirm action after DOM insert
    setTimeout(() => {
      const btn = document.getElementById('confirm-yes-btn');
      if (btn) btn.addEventListener('click', () => {
        this.close();
        onConfirm();
      });
    }, 50);
  },

  /**
   * Build a standard modal header HTML
   */
  buildHeader(title, iconPath) {
    return `
      <div class="modal-header">
        <div class="modal-header-left">
          <div class="modal-header-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">${iconPath}</svg>
          </div>
          <span class="modal-title">${title}</span>
        </div>
        <button class="modal-close-btn" onclick="Modal.close()">✕</button>
      </div>
    `;
  }
};
