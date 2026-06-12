// KVKK çerez onayı. v1 zorunlu olmayan çerez yüklemez; analytics bayrağı
// gelecekteki script'leri kapılamak için şimdiden saklanır.
const STORAGE_KEY = 'microline-consent-v1';
const banner = document.getElementById('cookie-consent');

function readConsent() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY));
    } catch {
        return null;
    }
}

function saveConsent(analytics) {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify({
            necessary: true,
            analytics,
            ts: Date.now()
        }));
    } catch {
        // localStorage kapalıysa (gizli mod vb.) banner her ziyarette görünür; kabul edilebilir
    }
}

if (banner && !readConsent()) {
    banner.hidden = false;
    // role="dialog": açılınca odak banner'a taşınmalı (WCAG); klavye
    // kullanıcısı sayfa sonundaki fixed banner'ı başka türlü fark edemez
    banner.querySelector('[data-consent]')?.focus();
    for (const button of banner.querySelectorAll('[data-consent]')) {
        button.addEventListener('click', () => {
            saveConsent(button.dataset.consent === 'all');
            banner.hidden = true;
        });
    }
}

// Gelecekteki analytics yüklemesi şu kalıpla kapılanır:
// const consent = readConsent(); if (consent?.analytics) { /* load script */ }
