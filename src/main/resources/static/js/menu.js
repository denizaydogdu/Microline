// Header etkileşimleri: Shop dropdown + mobil menü. CSP gereği harici modül (inline yok).

const setExpanded = (button, expanded) => {
  button.setAttribute('aria-expanded', String(expanded));
};

const initDropdowns = () => {
  const toggles = document.querySelectorAll('[data-dropdown-toggle]');

  toggles.forEach((toggle) => {
    const menu = document.getElementById(toggle.getAttribute('aria-controls'));
    if (!menu) return;

    const close = () => {
      menu.hidden = true;
      setExpanded(toggle, false);
    };

    toggle.addEventListener('click', () => {
      const willOpen = menu.hidden;
      menu.hidden = !willOpen;
      setExpanded(toggle, willOpen);
    });

    document.addEventListener('click', (event) => {
      if (menu.hidden) return;
      if (!toggle.contains(event.target) && !menu.contains(event.target)) {
        close();
      }
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && !menu.hidden) {
        close();
        toggle.focus();
      }
    });
  });
};

const initMobileMenu = () => {
  const toggle = document.querySelector('[data-menu-toggle]');
  const nav = toggle && document.getElementById(toggle.getAttribute('aria-controls'));
  if (!toggle || !nav) return;

  toggle.addEventListener('click', () => {
    const willOpen = !nav.classList.contains('is-open');
    nav.classList.toggle('is-open', willOpen);
    setExpanded(toggle, willOpen);
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && nav.classList.contains('is-open')) {
      nav.classList.remove('is-open');
      setExpanded(toggle, false);
      toggle.focus();
    }
  });
};

initDropdowns();
initMobileMenu();
