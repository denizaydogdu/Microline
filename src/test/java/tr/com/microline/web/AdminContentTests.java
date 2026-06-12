package tr.com.microline.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.entity.LegalPage;
import tr.com.microline.entity.Review;
import tr.com.microline.entity.TutorialPost;
import tr.com.microline.repository.LegalPageRepository;
import tr.com.microline.repository.ReviewRepository;
import tr.com.microline.repository.TutorialPostRepository;
import tr.com.microline.service.ReviewService;

/**
 * İçerik yönetimi (eğitim/yorum/yasal) uçtan uca. Test DB'si context'ler
 * arasında paylaşıldığı için her mutasyon testi tohum verisini sonunda eski
 * haline döndürür; eğitim/yorum testleri seed'e dokunmamak için kendi
 * kayıtlarını oluşturup siler.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class AdminContentTests {

    private static final Path UPLOADS_ROOT = Path.of("target/test-uploads").toAbsolutePath();

    /** ImageStorageTests ile aynı el yapımı asgari JPEG (sniff FF D8 FF'e bakar). */
    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 'J', 'F', 'I', 'F', 0x00,
            0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x04, 0x00, 0x01,
            (byte) 0xFF, (byte) 0xD9
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TutorialPostRepository tutorialPostRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private LegalPageRepository legalPageRepository;

    @Autowired
    private ReviewService reviewService;

    /** UserDetailsService'e dokunmadan ROLE_ADMIN kimliği basar. */
    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    private MockHttpServletRequestBuilder adminPost(String urlTemplate, Object... vars) {
        return post(urlTemplate, vars).with(admin()).with(csrf());
    }

    /** Tüm zorunlu alanları dolu, slug'ı BOŞ bırakılmış geçerli eğitim yükü (taslak). */
    private static MultiValueMap<String, String> validTutorialParams(String titleTr) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("slugTr", "");
        params.add("slugEn", "");
        params.add("titleTr", titleTr);
        params.add("excerptTr", "Test özeti");
        params.add("bodyTr", "<p>Test gövdesi</p>");
        params.add("difficulty", "BEGINNER");
        // published gönderilmez → null → taslak (wrapper Boolean davranışı)
        return params;
    }

    private TutorialPost requireBySlug(String slugTr) {
        return tutorialPostRepository.findAll().stream()
                .filter(t -> slugTr.equals(t.getSlugTr()))
                .findFirst().orElseThrow();
    }

    /* ── Render + güvenlik ──────────────────────────────────────────── */

    /** Thymeleaf hataları ancak render'da patlar — tüm GET sayfaları açılır. */
    @Test
    void contentPagesRender() throws Exception {
        TutorialPost seedTutorial = tutorialPostRepository
                .findBySlugTrAndPublishedTrue("ilk-kesiminiz-basit-kutu-ev").orElseThrow();
        LegalPage anyLegal = legalPageRepository.findAllByOrderByIdAsc().getFirst();

        mockMvc.perform(get("/admin/egitimler").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("İlk Kesiminiz")));
        mockMvc.perform(get("/admin/egitimler/yeni").with(admin()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/egitimler/{id}", seedTutorial.getId()).with(admin()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/yorumlar").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Ayşe K.")));
        mockMvc.perform(get("/admin/yasal-sayfalar").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("KVKK")));
        mockMvc.perform(get("/admin/yasal-sayfalar/{id}", anyLegal.getId()).with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousTutorialListRedirectsToAdminLogin() throws Exception {
        mockMvc.perform(get("/admin/egitimler"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/giris"));
    }

    /* ── Eğitimler ──────────────────────────────────────────────────── */

    @Test
    void createWithBlankSlugGeneratesTurkishSafeSlugAsDraft() throws Exception {
        mockMvc.perform(adminPost("/admin/egitimler/yeni")
                        .params(validTutorialParams("Hızlı Başlangıç Şablonu")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/egitimler/*"));

        // ı/ş → i/s: Türkçe harita + Locale.ROOT lowercase kanıtı
        TutorialPost created = requireBySlug("hizli-baslangic-sablonu");
        assertThat(created.isPublished()).isFalse();
        assertThat(created.getPublishedAt()).isNull();
        // titleEn boş bırakıldı → slugEn türetilmez, NULL kalır
        assertThat(created.getSlugEn()).isNull();

        tutorialPostRepository.delete(created);
    }

    /** İlk yayında damgalanır; kapat/aç döngüsü ORİJİNAL tarihi korur. */
    @Test
    void publishToggleStampsOnceAndKeepsOriginalDate() throws Exception {
        mockMvc.perform(adminPost("/admin/egitimler/yeni")
                        .params(validTutorialParams("Yayın Döngüsü Denemesi")))
                .andExpect(status().is3xxRedirection());
        TutorialPost post = requireBySlug("yayin-dongusu-denemesi");

        mockMvc.perform(adminPost("/admin/egitimler/{id}/durum", post.getId()))
                .andExpect(status().is3xxRedirection());
        TutorialPost published = tutorialPostRepository.findById(post.getId()).orElseThrow();
        assertThat(published.isPublished()).isTrue();
        Instant firstPublishedAt = published.getPublishedAt();
        assertThat(firstPublishedAt).isNotNull();

        // Yayından kaldır: tarih korunur
        mockMvc.perform(adminPost("/admin/egitimler/{id}/durum", post.getId()))
                .andExpect(status().is3xxRedirection());
        TutorialPost unpublished = tutorialPostRepository.findById(post.getId()).orElseThrow();
        assertThat(unpublished.isPublished()).isFalse();
        assertThat(unpublished.getPublishedAt()).isEqualTo(firstPublishedAt);

        // Yeniden yayınla: tarih ÜZERİNE YAZILMAZ (vitrin sıralaması stabil kalır)
        mockMvc.perform(adminPost("/admin/egitimler/{id}/durum", post.getId()))
                .andExpect(status().is3xxRedirection());
        TutorialPost republished = tutorialPostRepository.findById(post.getId()).orElseThrow();
        assertThat(republished.isPublished()).isTrue();
        assertThat(republished.getPublishedAt()).isEqualTo(firstPublishedAt);

        tutorialPostRepository.delete(republished);
    }

    @Test
    void coverUploadStoresFileAndDeleteRemovesRowWithFile() throws Exception {
        mockMvc.perform(multipart("/admin/egitimler/yeni")
                        .file(new MockMultipartFile("coverImage", "kapak görseli.jpeg", "image/jpeg", JPEG_BYTES))
                        .params(validTutorialParams("Kapaklı Eğitim Denemesi"))
                        .with(admin()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        TutorialPost created = requireBySlug("kapakli-egitim-denemesi");
        assertThat(created.getCoverImageUrl()).startsWith("/uploads/tutorial/");
        Path stored = UPLOADS_ROOT.resolve(created.getCoverImageUrl().substring("/uploads/".length()));
        assertThat(stored).exists();

        // Hard delete: satır + dosya birlikte gider
        mockMvc.perform(adminPost("/admin/egitimler/{id}/sil", created.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(tutorialPostRepository.findById(created.getId())).isEmpty();
        assertThat(Files.notExists(stored)).isTrue();
    }

    @Test
    void duplicateTutorialSlugReRendersFormWithoutSaving() throws Exception {
        long countBefore = tutorialPostRepository.count();
        MultiValueMap<String, String> params = validTutorialParams("Kopya Slug Denemesi");
        params.set("slugTr", "ilk-kesiminiz-basit-kutu-ev"); // V3 seed slug'ı

        mockMvc.perform(adminPost("/admin/egitimler/yeni").params(params))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("tutorialForm", "slugTr"));

        assertThat(tutorialPostRepository.count()).isEqualTo(countBefore);
    }

    /* ── Yorum moderasyonu (seed'e dokunmadan kendi kaydıyla) ────────── */

    @Test
    void reviewModerationLifecycle() throws Exception {
        Review review = new Review();
        review.setAuthorName("Test Moderasyon Yazarı");
        review.setRating((short) 5);
        review.setBody("Moderasyon testi yorumu — vitrine düşmemeli.");
        review.setLocale("tr");
        review.setApproved(false);
        review.setFeatured(false);
        review.setSource("test");
        review = reviewRepository.save(review);
        Long id = review.getId();
        Locale tr = Locale.forLanguageTag("tr");

        assertThat(reviewService.approved(tr))
                .noneMatch(r -> r.getId().equals(id));

        // Onay toggle'ı public listeye yansır
        mockMvc.perform(adminPost("/admin/yorumlar/{id}/onay", id))
                .andExpect(status().is3xxRedirection());
        assertThat(reviewService.approved(tr)).anyMatch(r -> r.getId().equals(id));

        // Onayı geri al → tekrar onaysız
        mockMvc.perform(adminPost("/admin/yorumlar/{id}/onay", id))
                .andExpect(status().is3xxRedirection());
        assertThat(reviewRepository.findById(id).orElseThrow().isApproved()).isFalse();

        // Onaysızken öne çıkar: featured + approved birlikte açılır
        mockMvc.perform(adminPost("/admin/yorumlar/{id}/one-cikar", id))
                .andExpect(status().is3xxRedirection());
        Review featured = reviewRepository.findById(id).orElseThrow();
        assertThat(featured.isFeatured()).isTrue();
        assertThat(featured.isApproved()).isTrue();
        assertThat(reviewService.featured(tr)).anyMatch(r -> r.getId().equals(id));

        // Hard delete
        mockMvc.perform(adminPost("/admin/yorumlar/{id}/sil", id))
                .andExpect(status().is3xxRedirection());
        assertThat(reviewRepository.findById(id)).isEmpty();
    }

    /* ── Yasal sayfa editörü (uçtan uca: panel → vitrin) ─────────────── */

    @Test
    void legalBodyUpdateShowsOnPublicPage() throws Exception {
        LegalPage page = legalPageRepository.findBySlugTr("garanti").orElseThrow();
        String originalBodyTr = page.getBodyTr();
        String marker = "Güncellenmiş garanti metni QA-784512";

        mockMvc.perform(adminPost("/admin/yasal-sayfalar/{id}", page.getId())
                        .param("titleTr", page.getTitleTr())
                        .param("titleEn", page.getTitleEn() == null ? "" : page.getTitleEn())
                        .param("bodyTr", "<p>" + marker + "</p>")
                        .param("bodyEn", page.getBodyEn() == null ? "" : page.getBodyEn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/yasal-sayfalar/*"));

        assertThat(legalPageRepository.findById(page.getId()).orElseThrow().getBodyTr())
                .contains(marker);

        // Uçtan uca kanıt: değişiklik public fallback rotasında render edilir
        mockMvc.perform(get("/tr/garanti"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString(marker)));

        // Seed gövdesine dönülür: diğer testler/yasal içerik bozulmasın
        LegalPage reloaded = legalPageRepository.findById(page.getId()).orElseThrow();
        reloaded.setBodyTr(originalBodyTr);
        legalPageRepository.save(reloaded);
    }

    @Test
    void legalFormValidationRejectsBlankBody() throws Exception {
        LegalPage page = legalPageRepository.findBySlugTr("garanti").orElseThrow();
        String originalBodyTr = page.getBodyTr();

        mockMvc.perform(adminPost("/admin/yasal-sayfalar/{id}", page.getId())
                        .param("titleTr", page.getTitleTr())
                        .param("titleEn", "")
                        .param("bodyTr", "")
                        .param("bodyEn", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("legalPageForm", "bodyTr"));

        assertThat(legalPageRepository.findById(page.getId()).orElseThrow().getBodyTr())
                .isEqualTo(originalBodyTr);
    }
}
