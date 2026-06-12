// Admin kabuk etkileşimleri: sidebar çekmecesi + tehlikeli işlem onayı.
// CSP 'self' gereği harici modül (inline yok); menu.js kalıbı izlenir.

const setExpanded = (button, expanded) => {
  button.setAttribute('aria-expanded', String(expanded));
};

const initSidebarToggle = () => {
  const toggle = document.querySelector('[data-menu-toggle]');
  const sidebar = toggle && document.getElementById(toggle.getAttribute('aria-controls'));
  if (!toggle || !sidebar) return;

  toggle.addEventListener('click', () => {
    const willOpen = !sidebar.classList.contains('is-open');
    sidebar.classList.toggle('is-open', willOpen);
    setExpanded(toggle, willOpen);
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && sidebar.classList.contains('is-open')) {
      sidebar.classList.remove('is-open');
      setExpanded(toggle, false);
      toggle.focus();
    }
  });
};

// Onay mesajı i18n: CSP inline onclick'e izin vermez — metin body data
// attribute'undan okunur, [data-confirm] formları submit'te sorar.
const initConfirmForms = () => {
  const message = document.body.dataset.msgConfirm;
  if (!message) return;

  document.addEventListener('submit', (event) => {
    const form = event.target;
    if (form instanceof HTMLFormElement && form.matches('[data-confirm]') && !window.confirm(message)) {
      event.preventDefault();
    }
  });
};

initSidebarToggle();
initConfirmForms();
