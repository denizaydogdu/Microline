# Microline — CLAUDE.md

## Proje Özeti

Microline, çocuk güvenli karton kesim makinesi satan, Türkçe-öncelikli (İngilizce seçmeli) bir tanıtım + sipariş sitesidir. Beaverbot (getbeaverbot.com) bilgi mimarisi temel alınmıştır.

**v1 kapsamı:** Vitrin + session-tabanlı sepet + sipariş/iletişim formları. **Online ödeme YOK, üyelik YOK.** Bunlar v2'de gelecek — şema engellemiyor (fiyatlar NUMERIC + currency; sipariş satırları `order_request_items`'ta talep anı `unit_price` snapshot'ıyla). Sepet `@SessionScope Cart` bean'inde yalnız ID+adet tutar; restart/timeout sepeti boşaltır (ödemesiz talep akışı için kabul edilmiş).

## Build & Çalıştırma

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # JDK 21 ZORUNLU (sistem varsayılanı 17!)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw test
./mvnw verify
```

- **Her zaman Maven Wrapper (`./mvnw`) kullan** — sistem `mvn` 3.6.3 ve eski.
- **Docker KULLANILMAZ.** PostgreSQL yerel Homebrew kurulumudur:
  - Başlat: `brew services start postgresql@14`
  - Dev DB: `microline` / test DB: `microline_test` (rol: `microline`, şifre: `microline`, localhost:5432)
- Testler `microline_test` veritabanını kullanır (`application-test.yml`, `@ActiveProfiles("test")`). Testcontainers YOK.

## Mimari

- Spring Boot 3.5 + Thymeleaf (sunucu taraflı render), tek modül, base package `tr.com.microline`
- Akış: controller → service → repository (Spring Data JPA) → PostgreSQL
- **Şemanın sahibi Flyway'dir**: `ddl-auto=validate`, asla `update` yapma. Migration'lar `src/main/resources/db/migration/V<n>__<ad>.sql`
- Paketler: `config` (Web/I18n/Security/Jpa), `i18n` (PathLocaleResolver, PageRoute), `controller`, `service`, `repository`, `entity`, `dto`
- Thymeleaf layout-dialect: `templates/layout/base.html` dekoratör; `templates/fragments/` header/footer/cookie-consent/components

## i18n Kuralları (Türkçe-öncelikli)

1. **Türkçe varsayılan ve fallback dildir.** `i18n/messages.properties` = TÜRKÇE dosyadır; `messages_en.properties` İngilizce.
2. Her sayfa hem `/tr/...` hem `/en/...` altında yaşar. Dil URL yolundan çözülür (`PathLocaleResolver`) — cookie/session YOK.
3. Yeni statik sayfa eklemek = `PageRoute` enum'una değer ekle + controller'da çift mapping (`@GetMapping({"/tr/<tr-slug>", "/en/<en-slug>"})`) + her iki bundle'a mesaj anahtarları. Sitemap ve hreflang otomatik takip eder.
4. Entity'lerde çevrilebilir alanlar `_tr`/`_en` kolon çiftidir; `_tr NOT NULL` (Türkçe zorunlu, İngilizce opsiyonel).
5. Şablonlarda görünür metni ASLA hardcode etme — `#{anahtar}` veya entity alanı kullan.

## İçerik Yerleşim Kuralı

- **`messages*.properties`**: nav/buton/form etiketleri, validasyon mesajları, statik sayfa meta title/description, ≤1 cümlelik pazarlama metinleri.
- **Veritabanı (Flyway seed)**: uzun yasal HTML, eğitim içerikleri, ürün açıklamaları/teknik özellikler, koleksiyon açıklamaları, yorumlar.
- **Kural:** Metin bir cümleden uzunsa, HTML içeriyorsa veya deploy olmadan düzenlenecekse → DB. Aksi halde → properties.

## Rota Tablosu

| Sayfa | TR | EN |
|---|---|---|
| Ana sayfa | `/tr/` | `/en/` |
| Ürün | `/tr/urun/{slug}` | `/en/products/{slug}` |
| Koleksiyon | `/tr/koleksiyon/{slug}` | `/en/collections/{slug}` |
| Yorumlar | `/tr/yorumlar` | `/en/reviews` |
| Eğitimler | `/tr/egitimler[/{slug}]` | `/en/tutorials[/{slug}]` |
| Eğitmenler | `/tr/egitimciler` | `/en/educators` |
| Hakkımızda | `/tr/hakkimizda` | `/en/about` |
| Ürün güvenliği | `/tr/urun-guvenligi` | `/en/product-safety` |
| Yeşil gelecek | `/tr/yesil-gelecek` | `/en/greener-future` |
| Ortaklık | `/tr/ortaklik-programi` | `/en/affiliate-program` |
| İletişim | `/tr/iletisim` | `/en/contact` |
| Sepet | `/tr/sepet` | `/en/cart` |
| Sepet ekle/güncelle/kaldır (POST) | `/tr/sepet/{ekle,guncelle,kaldir}` | `/en/cart/{add,update,remove}` |
| Checkout (sipariş talebi) | `/tr/siparis-talebi` | `/en/order-request` |
| Bülten (POST) | `/tr/bulten-abonelik` | `/en/newsletter` |
| Yasal (9 sayfa, DB) | `/tr/{slug}` fallback | `/en/{slug}` fallback |
| SEO | `/sitemap.xml`, `/robots.txt` (dilsiz) | |

- `/` → 302 → `/tr/`
- Yasal sayfalar `LegalPageController`'daki `@GetMapping("/{lang:tr|en}/{slug}")` fallback'inden servis edilir; literal pattern'lar her zaman önce eşleşir.
- **GET asla sepeti değiştirmez** — tüm sepet mutasyonları CSRF'li POST'tur. Checkout satırları formdan değil session sepetinden alır (tamper yüzeyi yok). Eski `?urun=slug` derin linkleri ürün sayfasına 302 yapar.

## Tasarım Sistemi Kuralları

- Framework YOK, build adımı YOK. Saf CSS + vanilla JS (ES modules, `defer`).
- CSS katman sırası: `tokens.css` → `base.css` → `layout.css` → `components.css` → `pages.css`
- **Tüm boşluklar 8'in katı** ve `--space-*` token'larından gelir. Inline style YASAK.
- Notion-vari nötr palet + tek marka accent; 1.25 oranlı tip ölçeği; tutarlı radius/gölge token'ları.
- Max-width 1200px container; mobile-first responsive.

## Veritabanı Konvansiyonları

- Migration adı: `V<n>__snake_case_aciklama.sql`. Seed'ler ana zincirde (vitrin sitesinde seed = prod içerik).
- Uzun HTML seed metinleri dollar-quoted: `$body$...$body$`
- Yasal sayfa seed'leri yer tutucudur, `<!-- LAWYER REVIEW REQUIRED -->` ile işaretlidir — **yayın öncesi Türk e-ticaret avukatı incelemesi şart**.
- Her slug kolonu unique index'li; rating 1–5 check constraint.

## Kod Konvansiyonları

- Lombok YOK. DTO'lar Java 21 record'u; constructor injection; form binding daima DTO'ya (asla entity'ye değil).
- CSRF açık (Thymeleaf `th:action` token'ı otomatik basar). Form spam koruması: honeypot + min-submit-time + IP throttle (captcha yok).
- Yorum yazarken: koddan anlaşılmayan kısıtları yaz, "ne yaptığını" anlatma.

## Süreklilik

Faz durumu `thoughts/ledgers/CONTINUITY_CLAUDE-microline-site.md` dosyasında checkbox'larla izlenir. Faz bitince checkbox'ı hemen güncelle.
