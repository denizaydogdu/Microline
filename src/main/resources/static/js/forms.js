// Progressive enhancement: çift gönderim engeli + tarayıcı doğrulama
// mesajlarının yerelleştirilmesi. Form JS'siz de çalışır (sunucu doğrular).

// --- Çift gönderim: disabled'ı senkron vermek bazı tarayıcılarda submit'i
// iptal ettiği, setTimeout ise hızlı çift tıklamaya yetişemediği için bayrak.
for (const form of document.querySelectorAll('form')) {
    let submitting = false;
    form.addEventListener('submit', (event) => {
        if (submitting) {
            event.preventDefault();
            return;
        }
        submitting = true;
        const button = form.querySelector('button[type="submit"]');
        if (button) {
            setTimeout(() => { button.disabled = true; }, 0);
        }
    });

    // Geri tuşu: Safari/Firefox sayfayı bfcache'ten dondurulmuş heap'le geri
    // yükler — bayrak sıfırlanmazsa form sayfa yenilenene dek gönderilemez
    window.addEventListener('pageshow', (event) => {
        if (event.persisted) {
            submitting = false;
            const button = form.querySelector('button[type="submit"]');
            if (button) {
                button.disabled = false;
            }
        }
    });
}

// --- Yerelleştirilmiş doğrulama balonları: tarayıcının "Please fill out this
// field" gibi kendi dilindeki mesajları, sayfanın dilindeki çevirilerle
// (body data-msg-*) değiştirilir. invalid bubbling yapmaz → capture fazı.
const msgs = document.body.dataset;

function localizedMessage(el) {
    const v = el.validity;
    if (v.valueMissing) {
        return el.type === 'checkbox' ? msgs.msgKvkk : msgs.msgRequired;
    }
    if (v.typeMismatch && el.type === 'email') {
        return msgs.msgEmail;
    }
    if (v.badInput || v.rangeUnderflow || v.rangeOverflow || v.stepMismatch) {
        return msgs.msgQty;
    }
    return msgs.msgRequired;
}

document.addEventListener('invalid', (event) => {
    const el = event.target;
    if (el.setCustomValidity && !el.validity.valid && !el.validity.customError) {
        const message = localizedMessage(el);
        if (message) {
            el.setCustomValidity(message);
        }
    }
}, true);

// Kullanıcı düzeltmeye başlayınca özel mesaj temizlenir; yoksa alan
// düzeltilse bile form gönderilemez (customError takılı kalır)
const clearCustomError = (event) => {
    if (event.target.setCustomValidity) {
        event.target.setCustomValidity('');
    }
};
document.addEventListener('input', clearCustomError, true);
// Otomatik doldurma (autofill) bazı yollarda input üretmez, change üretir
document.addEventListener('change', clearCustomError, true);
