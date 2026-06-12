# Microline

Çocuk güvenli karton kesim makinesi için Türkçe-öncelikli (İngilizce seçmeli) tanıtım ve sipariş sitesi.

- **Stack:** Spring Boot 3.5 · Thymeleaf · PostgreSQL 14 (yerel, Docker yok) · Java 21 · Maven
- **v1 kapsamı:** Vitrin + sipariş/iletişim formları (sepet ve online ödeme v2'de)

## Gereksinimler

- JDK 21 (`/usr/libexec/java_home -v 21`)
- PostgreSQL 14 (Homebrew): `brew services start postgresql@14`

## Çalıştırma

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# http://localhost:8080 → /tr/ adresine yönlenir
```

## Test

```bash
./mvnw test   # microline_test veritabanını kullanır
```

## Deploy: DigitalOcean droplet + nginx

Üretim `prod` profiliyle çalışır (`--spring.profiles.active=prod`):
uygulama yalnızca `127.0.0.1:8080`'e bind olur ve istemci IP'sini nginx'in
set ettiği `X-Forwarded-For`'dan okur. nginx örnek konfigürasyonu:

```nginx
server {
    listen 443 ssl http2;
    server_name www.microline.com.tr;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $remote_addr;   # zincir değil, tek IP
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Yerel/dev ortamında `forward-headers-strategy: none` varsayılandır —
doğrudan erişimde başlık taklidine kapalıdır.

Geliştirici kuralları ve mimari için `CLAUDE.md` dosyasına bakın.
