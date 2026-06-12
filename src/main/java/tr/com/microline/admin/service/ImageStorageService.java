package tr.com.microline.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

/**
 * Yüklenen görselleri yerel uploads dizinine yazar. Güvenlik modeli:
 * <ul>
 *   <li>Dosya adı DAİMA rastgele UUID — kullanıcı adı/uzantısı diske asla
 *       yansımaz (traversal + çakışma + script uzantısı kapanır).</li>
 *   <li>Uzantıyı beyan edilen content-type değil, magic-byte SNIFF belirler:
 *       .jpg adıyla HTML gömme (stored-XSS) işe yaramaz.</li>
 *   <li>Beyan ile sniff uyuşmazsa kabul etmek yerine REDDEDİLİR — tarayıcı
 *       content-sniffing sürprizlerine alan bırakılmaz.</li>
 * </ul>
 * Servis edilen URL'ler {@code /uploads/...} — Flyway seed görselleri
 * ({@code /img/...}, classpath) bu servisin kapsamı DIŞINDADIR.
 */
@Service
public class ImageStorageService {

    public enum ImageKind {
        PRODUCT("product"), COLLECTION("collection"), TUTORIAL("tutorial");

        private final String folder;

        ImageKind(String folder) {
            this.folder = folder;
        }

        public String folder() {
            return folder;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    /** spring.servlet.multipart.max-file-size ile aynı; servis ayrıca doğrular. */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final byte[] PNG_MAGIC =
            {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_MAGIC = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private final Path rootDir;

    public ImageStorageService(@Value("${microline.uploads-dir}") String uploadsDir) {
        this.rootDir = Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void createDirectories() {
        try {
            for (ImageKind kind : ImageKind.values()) {
                Files.createDirectories(rootDir.resolve(kind.folder()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Uploads dizini oluşturulamadı: " + rootDir, e);
        }
    }

    /**
     * Doğrular ve diske yazar.
     *
     * @return şablonların doğrudan kullanacağı URL: {@code /uploads/{folder}/{uuid}.{ext}}
     * @throws ImageStorageException boş dosya, boyut aşımı, izinsiz tür veya
     *         beyan/sniff uyuşmazlığında — hiçbir şey yazılmadan
     */
    public String store(MultipartFile file, ImageKind kind) {
        if (file == null || file.isEmpty()) {
            throw new ImageStorageException("admin.image.error.empty", "Görsel dosyası seçilmedi.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageStorageException("admin.image.error.tooLarge",
                    "Görsel 5MB'tan büyük olamaz.");
        }
        String declared = file.getContentType();
        if (declared == null || !ALLOWED_CONTENT_TYPES.contains(declared)) {
            throw new ImageStorageException("admin.image.error.type",
                    "Yalnızca JPEG, PNG veya WebP görselleri yüklenebilir.");
        }

        SniffedType sniffed = sniff(readHeader(file));
        if (sniffed == null || !sniffed.mime().equals(declared)) {
            // Sniff başarısız (görsel değil) ya da içerik beyanla çelişiyor —
            // ikisi de aynı kapıdan reddedilir, saldırgana ayrım sızdırılmaz.
            throw new ImageStorageException("admin.image.error.content",
                    "Dosya içeriği geçerli bir görsel değil.");
        }

        String filename = UUID.randomUUID() + "." + sniffed.extension();
        Path target = rootDir.resolve(kind.folder()).resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            throw new ImageStorageException("admin.image.error.io",
                    "Görsel kaydedilemedi.", e);
        }
        return "/uploads/" + kind.folder() + "/" + filename;
    }

    /**
     * Yalnızca bu servisin yazdığı dosyaları siler: {@code /uploads/} ile
     * başlamayan URL'ler (özellikle {@code /img/...} classpath seed'leri) ve
     * kök dizin dışına çözülen traversal denemeleri sessizce no-op'tur.
     */
    public void deleteIfUploaded(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        Path resolved = rootDir.resolve(url.substring("/uploads/".length())).normalize();
        if (!resolved.startsWith(rootDir)) {
            log.warn("Uploads kökü dışına çıkan silme isteği yok sayıldı: {}", url);
            return;
        }
        try {
            Files.deleteIfExists(resolved);
        } catch (IOException e) {
            // Silinemeyen yetim dosya sadece disk artığıdır; akışı kırmaya değmez.
            log.warn("Yüklenmiş görsel silinemedi: {}", resolved, e);
        }
    }

    private record SniffedType(String mime, String extension) {
    }

    /** İlk 12 bayt: en uzun imza WEBP (RIFF....WEBP) için yeterli. */
    private byte[] readHeader(MultipartFile file) {
        byte[] header = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.readNBytes(header, 0, header.length);
            return read == header.length ? header : java.util.Arrays.copyOf(header, read);
        } catch (IOException e) {
            throw new ImageStorageException("admin.image.error.io",
                    "Görsel okunamadı.", e);
        }
    }

    private SniffedType sniff(byte[] header) {
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return new SniffedType("image/jpeg", "jpg");
        }
        if (matchesAt(header, 0, PNG_MAGIC)) {
            return new SniffedType("image/png", "png");
        }
        if (matchesAt(header, 0, RIFF_MAGIC) && matchesAt(header, 8, WEBP_MAGIC)) {
            return new SniffedType("image/webp", "webp");
        }
        return null;
    }

    private boolean matchesAt(byte[] data, int offset, byte[] expected) {
        if (data.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
