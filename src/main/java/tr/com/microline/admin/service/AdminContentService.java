package tr.com.microline.admin.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import tr.com.microline.admin.dto.LegalPageForm;
import tr.com.microline.admin.dto.TutorialForm;
import tr.com.microline.admin.service.ImageStorageService.ImageKind;
import tr.com.microline.entity.LegalPage;
import tr.com.microline.entity.Review;
import tr.com.microline.entity.TutorialPost;
import tr.com.microline.i18n.Slugs;
import tr.com.microline.repository.LegalPageRepository;
import tr.com.microline.repository.ReviewRepository;
import tr.com.microline.repository.TutorialPostRepository;

/**
 * İçerik yazma tarafı (eğitimler, yorum moderasyonu, yasal sayfalar).
 * Public TutorialService/ReviewService/LegalPageService readOnly kalır ve
 * buradan ASLA çağrılmaz. Katalog tarafıyla aynı kurallar:
 * <ul>
 *   <li>Form→entity kopyalama yalnız burada (entity'ye binding yok).</li>
 *   <li>Slug boşsa başlıktan üretilir ({@link Slugs}); benzersizlik
 *       ihlalleri BindingResult alan hatasına çevrilir.</li>
 *   <li>Kapak değişiminde eski dosya kayıt BAŞARIYLA güncellendikten sonra
 *       silinir; {@code /img/...} seed'lerine deleteIfUploaded dokunmaz.</li>
 *   <li>Eğitim ve yorum HARD delete edilebilir (FK'siz). Yasal sayfalarda
 *       yalnız başlık/gövde düzenlenir — create/delete/slug değişimi yok.</li>
 * </ul>
 * Yayın tarihi kuralı: publishedAt yalnız İLK yayına geçişte damgalanır;
 * yayından kaldırmak ve yeniden yayınlamak orijinal tarihi korur (vitrin
 * sıralaması "ilk yayın" tarihine göre stabil kalır).
 */
@Service
public class AdminContentService {

    private final TutorialPostRepository tutorialPostRepository;
    private final ReviewRepository reviewRepository;
    private final LegalPageRepository legalPageRepository;
    private final ImageStorageService imageStorageService;

    public AdminContentService(TutorialPostRepository tutorialPostRepository,
                               ReviewRepository reviewRepository,
                               LegalPageRepository legalPageRepository,
                               ImageStorageService imageStorageService) {
        this.tutorialPostRepository = tutorialPostRepository;
        this.reviewRepository = reviewRepository;
        this.legalPageRepository = legalPageRepository;
        this.imageStorageService = imageStorageService;
    }

    /* ── Eğitimler ───────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<TutorialPost> tutorials() {
        return tutorialPostRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<TutorialPost> tutorial(Long id) {
        return tutorialPostRepository.findById(id);
    }

    /**
     * Create (form.id == null) ve update'i tek yoldan geçirir. Kapak dosyası
     * yalnız form tamamen geçerliyse diske yazılır; {@link ImageStorageException}
     * çağırana yükselir — controller alan hatasına çevirir.
     *
     * @return kaydedilen eğitimin id'si; doğrulama hatasında {@code null}
     */
    @Transactional
    public Long saveTutorial(TutorialForm form, BindingResult bindingResult) {
        long excludeId = form.id() != null ? form.id() : -1L;
        String slugTr = resolveSlug(form.slugTr(), form.titleTr());
        String slugEn = resolveOptionalSlug(form.slugEn(), form.titleEn());

        if (slugTr.isBlank() && !bindingResult.hasFieldErrors("slugTr")
                && !bindingResult.hasFieldErrors("titleTr")) {
            bindingResult.rejectValue("slugTr", "admin.validation.slug", "Geçersiz slug.");
        }
        if (!slugTr.isBlank() && tutorialPostRepository.existsBySlugTrAndIdNot(slugTr, excludeId)) {
            bindingResult.rejectValue("slugTr", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (slugEn != null && tutorialPostRepository.existsBySlugEnAndIdNot(slugEn, excludeId)) {
            bindingResult.rejectValue("slugEn", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (bindingResult.hasErrors()) {
            return null;
        }

        TutorialPost post = form.id() != null
                ? tutorialPostRepository.findById(form.id()).orElseThrow(AdminContentService::notFound)
                : new TutorialPost();

        String oldCover = post.getCoverImageUrl();
        boolean coverReplaced = form.coverImage() != null && !form.coverImage().isEmpty();
        if (coverReplaced) {
            post.setCoverImageUrl(imageStorageService.store(form.coverImage(), ImageKind.TUTORIAL));
        }
        post.setSlugTr(slugTr);
        post.setSlugEn(slugEn);
        post.setTitleTr(form.titleTr());
        post.setTitleEn(nullIfBlank(form.titleEn()));
        post.setExcerptTr(form.excerptTr());
        post.setExcerptEn(nullIfBlank(form.excerptEn()));
        post.setBodyTr(form.bodyTr());
        post.setBodyEn(nullIfBlank(form.bodyEn()));
        post.setVideoUrl(nullIfBlank(form.videoUrl()));
        post.setDifficulty(form.difficulty());
        boolean publish = Boolean.TRUE.equals(form.published());
        post.setPublished(publish);
        if (publish && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
        Long savedId = tutorialPostRepository.save(post).getId();
        if (coverReplaced) {
            // Kayıt başarıyla yazıldıktan SONRA: hata yarıda keserse eski görsel kalır
            imageStorageService.deleteIfUploaded(oldCover);
        }
        return savedId;
    }

    @Transactional
    public void togglePublished(Long id) {
        TutorialPost post = tutorialPostRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        boolean publish = !post.isPublished();
        post.setPublished(publish);
        if (publish && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
        tutorialPostRepository.save(post);
    }

    /** Hard delete: satır + yüklenmiş kapak birlikte gider (seed /img/... kalır). */
    @Transactional
    public void deleteTutorial(Long id) {
        TutorialPost post = tutorialPostRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        tutorialPostRepository.delete(post);
        imageStorageService.deleteIfUploaded(post.getCoverImageUrl());
    }

    /* ── Yorumlar ────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Review> reviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void toggleApproved(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        review.setApproved(!review.isApproved());
        reviewRepository.save(review);
    }

    /**
     * Öne çıkarmak onayı da kapsar: vitrin yalnız featured+approved basar,
     * onaysız bir yorumu öne çıkarmak aksi halde sessizce hiçbir şey
     * yapmazdı. Geri alma onaya dokunmaz (moderasyon kararı ayrı kalır).
     */
    @Transactional
    public void toggleFeatured(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        boolean featured = !review.isFeatured();
        review.setFeatured(featured);
        if (featured) {
            review.setApproved(true);
        }
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        reviewRepository.delete(review);
    }

    /* ── Yasal sayfalar ──────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<LegalPage> legalPages() {
        return legalPageRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<LegalPage> legalPage(Long id) {
        return legalPageRepository.findById(id);
    }

    /** Yalnız başlık + gövde; code/slug bilinçli olarak dokunulmaz kalır. */
    @Transactional
    public void updateLegalPage(Long id, LegalPageForm form) {
        LegalPage page = legalPageRepository.findById(id)
                .orElseThrow(AdminContentService::notFound);
        page.setTitleTr(form.titleTr());
        page.setTitleEn(nullIfBlank(form.titleEn()));
        page.setBodyTr(form.bodyTr());
        page.setBodyEn(nullIfBlank(form.bodyEn()));
        legalPageRepository.save(page);
    }

    /* ── Yardımcılar (AdminCatalogService ile aynı kurallar) ─────────── */

    private static String resolveSlug(String slug, String title) {
        return (slug == null || slug.isBlank()) ? Slugs.slugify(title) : slug;
    }

    /** EN slug: boş + EN başlık da boşsa NULL kalır (unique index null'ları saymaz). */
    private static String resolveOptionalSlug(String slug, String title) {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        String derived = Slugs.slugify(title);
        return derived.isBlank() ? null : derived;
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private static String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
