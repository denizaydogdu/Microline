package tr.com.microline.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.admin.service.ImageStorageException;
import tr.com.microline.admin.service.ImageStorageService;
import tr.com.microline.admin.service.ImageStorageService.ImageKind;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class ImageStorageTests {

    private static final Path UPLOADS_ROOT = Path.of("target/test-uploads").toAbsolutePath();

    /**
     * Elle örülmüş asgari JPEG: sniff için FF D8 FF yeterli, ama gerçekçi
     * olsun diye JFIF başlığı + EOI (FF D9) de var. Decode edilebilir olması
     * gerekmez — servis yalnızca magic-byte'lara bakar.
     */
    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 'J', 'F', 'I', 'F', 0x00,
            0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x04, 0x00, 0x01,
            (byte) 0xFF, (byte) 0xD9
    };

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 'I', 'H', 'D', 'R',
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01
    };

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private MockMvc mockMvc;

    private static MockMultipartFile multipart(String name, String contentType, byte[] bytes) {
        return new MockMultipartFile("file", name, contentType, bytes);
    }

    private static long entryCount(Path dir) {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void storesJpegUnderProductFolderWithUuidName() {
        String url = imageStorageService.store(
                multipart("katalog görseli.jpeg", "image/jpeg", JPEG_BYTES), ImageKind.PRODUCT);

        // Uzantı .jpg (sniff'ten), ad UUID — kullanıcı dosya adından iz yok
        assertThat(url).matches("/uploads/product/[0-9a-f-]{36}\\.jpg");
        Path stored = UPLOADS_ROOT.resolve("product").resolve(url.substring("/uploads/product/".length()));
        assertThat(stored).exists();
    }

    @Test
    void storesPngWithPngExtension() {
        String url = imageStorageService.store(
                multipart("kapak.png", "image/png", PNG_BYTES), ImageKind.TUTORIAL);

        assertThat(url).matches("/uploads/tutorial/[0-9a-f-]{36}\\.png");
        assertThat(UPLOADS_ROOT.resolve(url.substring("/uploads/".length()))).exists();
    }

    @Test
    void rejectsHtmlBytesDeclaredAsJpegWithoutWritingAnything() {
        // Stored-XSS denemesi: beyan image/jpeg ama içerik HTML — sniff yakalar
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);
        long before = entryCount(UPLOADS_ROOT.resolve("product"));

        assertThatExceptionOfType(ImageStorageException.class)
                .isThrownBy(() -> imageStorageService.store(
                        multipart("zararsiz.jpg", "image/jpeg", html), ImageKind.PRODUCT))
                .satisfies(e -> assertThat(e.getMessageKey()).isEqualTo("admin.image.error.content"));

        assertThat(entryCount(UPLOADS_ROOT.resolve("product"))).isEqualTo(before);
    }

    @Test
    void rejectsDeclaredContentTypeOutsideAllowlist() {
        assertThatExceptionOfType(ImageStorageException.class)
                .isThrownBy(() -> imageStorageService.store(
                        multipart("sayfa.html", "text/html", JPEG_BYTES), ImageKind.PRODUCT))
                .satisfies(e -> assertThat(e.getMessageKey()).isEqualTo("admin.image.error.type"));
    }

    @Test
    void rejectsDeclaredTypeMismatchingSniffedContent() {
        // İkisi de allowlist'te ama beyan (png) içerikle (jpeg) çelişiyor
        assertThatExceptionOfType(ImageStorageException.class)
                .isThrownBy(() -> imageStorageService.store(
                        multipart("resim.png", "image/png", JPEG_BYTES), ImageKind.COLLECTION))
                .satisfies(e -> assertThat(e.getMessageKey()).isEqualTo("admin.image.error.content"));
    }

    @Test
    void rejectsOversizeFileBeforeContentChecks() {
        byte[] sixMb = new byte[6 * 1024 * 1024];

        assertThatExceptionOfType(ImageStorageException.class)
                .isThrownBy(() -> imageStorageService.store(
                        multipart("dev.jpg", "image/jpeg", sixMb), ImageKind.PRODUCT))
                .satisfies(e -> assertThat(e.getMessageKey()).isEqualTo("admin.image.error.tooLarge"));
    }

    @Test
    void rejectsEmptyFile() {
        assertThatExceptionOfType(ImageStorageException.class)
                .isThrownBy(() -> imageStorageService.store(
                        multipart("bos.jpg", "image/jpeg", new byte[0]), ImageKind.PRODUCT))
                .satisfies(e -> assertThat(e.getMessageKey()).isEqualTo("admin.image.error.empty"));
    }

    @Test
    void deleteIfUploadedRemovesStoredFile() {
        String url = imageStorageService.store(
                multipart("silinecek.jpg", "image/jpeg", JPEG_BYTES), ImageKind.COLLECTION);
        Path stored = UPLOADS_ROOT.resolve(url.substring("/uploads/".length()));
        assertThat(stored).exists();

        imageStorageService.deleteIfUploaded(url);

        assertThat(stored).doesNotExist();
    }

    @Test
    void deleteIfUploadedIgnoresClasspathSeedImages() {
        assertThatCode(() -> imageStorageService.deleteIfUploaded("/img/product/microline-1.jpg"))
                .doesNotThrowAnyException();
        // Seed görseli classpath'te duruyor (servis ona hiç dokunamaz zaten)
        assertThat(new ClassPathResource("static/img/product/microline-1.jpg").exists()).isTrue();
    }

    @Test
    void deleteIfUploadedBlocksPathTraversal() throws IOException {
        // Kök DIŞINA nöbetçi dosya: traversal koruması delinirse silinirdi
        Path sentinel = UPLOADS_ROOT.getParent().resolve("traversal-sentinel.txt");
        Files.writeString(sentinel, "dokunma");
        try {
            assertThatCode(() -> {
                imageStorageService.deleteIfUploaded("/uploads/../traversal-sentinel.txt");
                imageStorageService.deleteIfUploaded("/uploads/../../etc/passwd");
            }).doesNotThrowAnyException();
            assertThat(sentinel).exists();
        } finally {
            Files.deleteIfExists(sentinel);
        }
    }

    /* --- Resource handler + public chain --- */

    @Test
    void storedImageIsServedPubliclyWithImageContentType() throws Exception {
        String url = imageStorageService.store(
                multipart("servis.jpg", "image/jpeg", JPEG_BYTES), ImageKind.PRODUCT);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(JPEG_BYTES));
    }

    @Test
    void missingUploadReturns404NotAuthRedirect() throws Exception {
        // /uploads/** public zincire düşer: 404 beklenir, 401/302 giriş DEĞİL
        mockMvc.perform(get("/uploads/yok.jpg"))
                .andExpect(status().isNotFound());
    }
}
