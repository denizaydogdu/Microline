-- Code review bulguları: sık sorgulanan kolonlara indeks + KVKK denetim güvencesi.

-- Yorumlar sayfası her istekte approved+locale ile filtreler
CREATE INDEX idx_reviews_approved_locale ON reviews (approved, locale, created_at DESC);

-- Eğitim hub'ı published + tarih sıralı listeler
CREATE INDEX idx_tutorials_published_date ON tutorial_posts (published, published_at DESC);

-- Koleksiyon sayfası ürünleri collection+active ile çeker
CREATE INDEX idx_products_collection_active ON products (collection_id, active, sort_order);

-- Uygulama katmanı dışından (ileride admin/script) rızasız kayıt yazılamasın:
-- KVKK denetiminde "rıza olmadan kayıt var mı" sorusunun cevabı şemada hayır olsun
ALTER TABLE order_requests
    ADD CONSTRAINT ck_order_requests_kvkk CHECK (kvkk_consent = TRUE);

ALTER TABLE contact_messages
    ADD CONSTRAINT ck_contact_messages_kvkk CHECK (kvkk_consent = TRUE);
