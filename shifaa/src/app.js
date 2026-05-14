/* =========================================
   SHIFAA - Main App Controller
   src/app.js
   ========================================= */

const App = {

  currentPage: 'evenements',

  /**
   * Initialize the application
   */
  init() {
    // Mount initial page
    EventsPage.mount();

    // Setup sidebar navigation
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = item.getAttribute('data-page');
        this.navigate(page);
      });
    });
  },

  /**
   * Navigate to a page
   * @param {string} page  - 'evenements' | 'participants' | etc.
   * @param {string} preset - optional filter preset (e.g. 'ev1')
   */
  navigate(page, preset = null) {
    // Deactivate all sidebar items
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    // Activate target nav item
    const navItem = document.querySelector(`.nav-item[data-page="${page}"]`);
    if (navItem) navItem.classList.add('active');

    // Hide all pages
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));

    // Show target page and mount it
    const pageEl = document.getElementById(`page-${page}`);
    if (pageEl) pageEl.classList.add('active');

    this.currentPage = page;

    // Mount the right page controller
    switch (page) {
      case 'evenements':
        EventsPage.mount();
        break;
      case 'participants':
        ParticipantsPage.mount(preset);
        break;
      default:
        // For other pages (users, dossiers, reclamations) - placeholder
        const root = pageEl?.querySelector('[id$="-root"]') || pageEl;
        if (root) root.innerHTML = `
          <div style="padding:60px;text-align:center;color:var(--text-light)">
            <div style="font-size:48px;margin-bottom:16px">🚧</div>
            <p style="font-size:16px">Module en construction...</p>
          </div>`;
    }
  }
};

// ---- Boot ---- //
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
