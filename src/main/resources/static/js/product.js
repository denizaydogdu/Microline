// Ürün sayfası ilerlemeli iyileştirmeleri: galeri küçük resim geçişi +
// varyant seçiminde fiyat güncelleme. JS'siz de sayfa tam çalışır (ilk
// görsel ve baz fiyat sunucuda render edilir); fiyat metni sunucuda
// biçimlendiği için istemcide para birimi mantığı yoktur.

const initGallery = () => {
  const mainImg = document.getElementById('gallery-main-img');
  const thumbs = document.querySelectorAll('[data-gallery-thumb]');
  if (!mainImg || thumbs.length === 0) return;

  thumbs.forEach((thumb) => {
    thumb.addEventListener('click', () => {
      thumbs.forEach((t) => t.classList.remove('is-active'));
      thumb.classList.add('is-active');
      if (thumb.dataset.src) {
        mainImg.src = thumb.dataset.src;
        mainImg.alt = thumb.dataset.alt || '';
      }
    });
  });
};

const initVariantPricing = () => {
  const priceEl = document.getElementById('buy-price');
  const radios = document.querySelectorAll('input[name="variantId"][data-variant-price]');
  if (!priceEl || radios.length === 0) return;

  radios.forEach((radio) => {
    radio.addEventListener('change', () => {
      if (radio.checked) {
        priceEl.textContent = radio.dataset.variantPrice;
      }
    });
  });
};

initGallery();
initVariantPricing();
