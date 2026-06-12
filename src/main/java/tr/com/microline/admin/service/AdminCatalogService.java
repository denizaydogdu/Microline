package tr.com.microline.admin.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import tr.com.microline.admin.dto.CollectionForm;
import tr.com.microline.admin.dto.ImageMetaForm;
import tr.com.microline.admin.dto.ProductForm;
import tr.com.microline.admin.dto.ProductImageForm;
import tr.com.microline.admin.dto.VariantForm;
import tr.com.microline.admin.service.ImageStorageService.ImageKind;
import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductImage;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.i18n.Slugs;
import tr.com.microline.repository.CollectionRepository;
import tr.com.microline.repository.ProductImageRepository;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;

/**
 * Katalog yazma tarafı. Public ProductService/CollectionService readOnly
 * kalır ve buradan ASLA çağrılmaz. Kurallar:
 * <ul>
 *   <li>Form→entity kopyalama yalnız burada (entity'ye binding yok).</li>
 *   <li>Slug boşsa addan üretilir ({@link Slugs}); benzersizlik ihlalleri
 *       BindingResult alan hatasına çevrilir, exception fırlatılmaz.</li>
 *   <li>Ürün/varyant/koleksiyon silinmez — yalnız aktiflik toggle'ı
 *       (FK'ler cascadesiz). ProductImage hard delete edilebilir (CASCADE).</li>
 *   <li>Görsel değişiminde eski dosya kayıt BAŞARIYLA güncellendikten sonra
 *       silinir; {@code /img/...} seed'lerine deleteIfUploaded dokunmaz.</li>
 * </ul>
 */
@Service
public class AdminCatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CollectionRepository collectionRepository;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    public AdminCatalogService(ProductRepository productRepository,
                               ProductVariantRepository productVariantRepository,
                               ProductImageRepository productImageRepository,
                               CollectionRepository collectionRepository,
                               ImageStorageService imageStorageService,
                               ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productImageRepository = productImageRepository;
        this.collectionRepository = collectionRepository;
        this.imageStorageService = imageStorageService;
        this.objectMapper = objectMapper;
    }

    /* ── Koleksiyonlar ───────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Collection> collections() {
        return collectionRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Collection> collection(Long id) {
        return collectionRepository.findById(id);
    }

    /**
     * Doğrular ve kaydeder; hatalar {@code bindingResult}'a yazılır.
     * Hero dosyası yalnız form tamamen geçerliyse diske yazılır
     * (geçersiz formda yetim dosya oluşmaz); {@link ImageStorageException}
     * çağırana yükselir — controller alan hatasına çevirir.
     */
    @Transactional
    public void updateCollection(Long id, CollectionForm form, BindingResult bindingResult) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(AdminCatalogService::notFound);

        String slugTr = resolveSlug(form.slugTr(), form.nameTr());
        String slugEn = resolveOptionalSlug(form.slugEn(), form.nameEn());
        if (slugTr.isBlank() && !bindingResult.hasFieldErrors("slugTr")) {
            bindingResult.rejectValue("slugTr", "admin.validation.slug", "Geçersiz slug.");
        }
        if (!slugTr.isBlank() && collectionRepository.existsBySlugTrAndIdNot(slugTr, id)) {
            bindingResult.rejectValue("slugTr", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (slugEn != null && collectionRepository.existsBySlugEnAndIdNot(slugEn, id)) {
            bindingResult.rejectValue("slugEn", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        String oldHero = collection.getHeroImageUrl();
        boolean heroReplaced = form.heroImage() != null && !form.heroImage().isEmpty();
        if (heroReplaced) {
            collection.setHeroImageUrl(imageStorageService.store(form.heroImage(), ImageKind.COLLECTION));
        }
        collection.setSlugTr(slugTr);
        collection.setSlugEn(slugEn);
        collection.setNameTr(form.nameTr());
        collection.setNameEn(nullIfBlank(form.nameEn()));
        collection.setDescriptionTr(form.descriptionTr());
        collection.setDescriptionEn(nullIfBlank(form.descriptionEn()));
        collection.setSortOrder(form.sortOrder());
        collection.setActive(Boolean.TRUE.equals(form.active()));
        collectionRepository.save(collection);
        if (heroReplaced) {
            // Kayıt başarıyla yazıldıktan SONRA: hata yarıda keserse eski görsel kalır
            imageStorageService.deleteIfUploaded(oldHero);
        }
    }

    @Transactional
    public void toggleCollectionActive(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(AdminCatalogService::notFound);
        collection.setActive(!collection.isActive());
        collectionRepository.save(collection);
    }

    /* ── Ürünler ─────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Product> products() {
        return productRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Product> product(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Create (form.id == null) ve update'i tek yoldan geçirir.
     *
     * @return kaydedilen ürünün id'si; doğrulama hatasında {@code null}
     *         (hatalar bindingResult'ta)
     */
    @Transactional
    public Long saveProduct(ProductForm form, BindingResult bindingResult) {
        long excludeId = form.id() != null ? form.id() : -1L;
        String slugTr = resolveSlug(form.slugTr(), form.nameTr());
        String slugEn = resolveOptionalSlug(form.slugEn(), form.nameEn());

        if (slugTr.isBlank() && !bindingResult.hasFieldErrors("slugTr")
                && !bindingResult.hasFieldErrors("nameTr")) {
            bindingResult.rejectValue("slugTr", "admin.validation.slug", "Geçersiz slug.");
        }
        if (!slugTr.isBlank() && productRepository.existsBySlugTrAndIdNot(slugTr, excludeId)) {
            bindingResult.rejectValue("slugTr", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (slugEn != null && productRepository.existsBySlugEnAndIdNot(slugEn, excludeId)) {
            bindingResult.rejectValue("slugEn", "admin.validation.slugTaken", "Bu slug kullanımda.");
        }
        if (form.sku() != null && !form.sku().isBlank()
                && productRepository.existsBySkuAndIdNot(form.sku(), excludeId)) {
            bindingResult.rejectValue("sku", "admin.validation.skuTaken", "Bu SKU kullanımda.");
        }
        validateSpecsJson(form.specsJson(), bindingResult);
        if (bindingResult.hasErrors()) {
            return null;
        }

        Product product = form.id() != null
                ? productRepository.findById(form.id()).orElseThrow(AdminCatalogService::notFound)
                : new Product();
        Collection collection = collectionRepository.findById(form.collectionId())
                .orElseThrow(AdminCatalogService::notFound);

        product.setSku(form.sku());
        product.setSlugTr(slugTr);
        product.setSlugEn(slugEn);
        product.setNameTr(form.nameTr());
        product.setNameEn(nullIfBlank(form.nameEn()));
        product.setTaglineTr(form.taglineTr());
        product.setTaglineEn(nullIfBlank(form.taglineEn()));
        product.setDescriptionTr(form.descriptionTr());
        product.setDescriptionEn(nullIfBlank(form.descriptionEn()));
        // safety_notes_tr NOT NULL: boş bırakılırsa boş metin yazılır
        product.setSafetyNotesTr(form.safetyNotesTr() == null ? "" : form.safetyNotesTr());
        product.setSafetyNotesEn(nullIfBlank(form.safetyNotesEn()));
        product.setSpecsJson(nullIfBlank(form.specsJson()));
        product.setPriceAmount(form.priceAmount());
        product.setCompareAtPrice(form.compareAtPrice());
        product.setCollection(collection);
        product.setFeatured(Boolean.TRUE.equals(form.featured()));
        product.setActive(Boolean.TRUE.equals(form.active()));
        product.setSortOrder(form.sortOrder());
        product.setMetaDescriptionTr(form.metaDescriptionTr());
        product.setMetaDescriptionEn(nullIfBlank(form.metaDescriptionEn()));
        return productRepository.save(product).getId();
    }

    @Transactional
    public void toggleProductActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(AdminCatalogService::notFound);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    /* ── Varyantlar ──────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<ProductVariant> variants(Product product) {
        return productVariantRepository.findByProductOrderBySortOrderAscIdAsc(product);
    }

    /** @return başarıda true; SKU çakışmasında false (hata bindingResult'ta) */
    @Transactional
    public boolean addVariant(Long productId, VariantForm form, BindingResult bindingResult) {
        Product product = productRepository.findById(productId)
                .orElseThrow(AdminCatalogService::notFound);
        if (!checkVariantSku(form.sku(), -1L, bindingResult) || bindingResult.hasErrors()) {
            return false;
        }
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        copyVariant(form, variant);
        productVariantRepository.save(variant);
        return true;
    }

    @Transactional
    public boolean updateVariant(Long productId, Long variantId, VariantForm form,
                                 BindingResult bindingResult) {
        ProductVariant variant = ownedVariant(productId, variantId);
        if (!checkVariantSku(form.sku(), variantId, bindingResult) || bindingResult.hasErrors()) {
            return false;
        }
        copyVariant(form, variant);
        productVariantRepository.save(variant);
        return true;
    }

    @Transactional
    public void toggleVariantActive(Long productId, Long variantId) {
        ProductVariant variant = ownedVariant(productId, variantId);
        variant.setActive(!variant.isActive());
        productVariantRepository.save(variant);
    }

    /* ── Görseller ───────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<ProductImage> images(Product product) {
        return productImageRepository.findByProductOrderBySortOrder(product);
    }

    /**
     * Dosyayı diske yazıp satır açar; sıra mevcut en büyük sortOrder + 1.
     * {@link ImageStorageException} çağırana yükselir (dosya yazılmamıştır).
     */
    @Transactional
    public void addImage(Long productId, ProductImageForm form) {
        Product product = productRepository.findById(productId)
                .orElseThrow(AdminCatalogService::notFound);
        int nextOrder = productImageRepository.findByProductOrderBySortOrder(product).stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(0) + 1;
        String url = imageStorageService.store(form.file(), ImageKind.PRODUCT);
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setAltTr(form.altTr());
        image.setAltEn(nullIfBlank(form.altEn()));
        image.setSortOrder(nextOrder);
        productImageRepository.save(image);
    }

    @Transactional
    public void updateImageMeta(Long productId, Long imageId, ImageMetaForm form) {
        ProductImage image = ownedImage(productId, imageId);
        image.setAltTr(form.altTr());
        image.setAltEn(nullIfBlank(form.altEn()));
        image.setSortOrder(form.sortOrder());
        productImageRepository.save(image);
    }

    /** Satır + dosya birlikte gider; seed (/img/...) URL'lerinde yalnız satır silinir. */
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = ownedImage(productId, imageId);
        productImageRepository.delete(image);
        imageStorageService.deleteIfUploaded(image.getUrl());
    }

    /** Kapak ataması: ürün kartları/galeri hero'su bu URL'i basar. */
    @Transactional
    public void setHero(Long productId, Long imageId) {
        ProductImage image = ownedImage(productId, imageId);
        Product product = productRepository.findById(productId)
                .orElseThrow(AdminCatalogService::notFound);
        product.setHeroImageUrl(image.getUrl());
        productRepository.save(product);
    }

    /* ── Yardımcılar ─────────────────────────────────────────────────── */

    /** Slug boş bırakıldıysa addan üretilir; doluysa form pattern'i zaten doğruladı. */
    private static String resolveSlug(String slug, String name) {
        return (slug == null || slug.isBlank()) ? Slugs.slugify(name) : slug;
    }

    /** EN slug: boş + EN ad da boşsa NULL kalır (unique index null'ları saymaz). */
    private static String resolveOptionalSlug(String slug, String name) {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        String derived = Slugs.slugify(name);
        return derived.isBlank() ? null : derived;
    }

    private void validateSpecsJson(String specsJson, BindingResult bindingResult) {
        if (specsJson == null || specsJson.isBlank()) {
            return;
        }
        try {
            objectMapper.readTree(specsJson);
        } catch (JacksonException e) {
            bindingResult.rejectValue("specsJson", "admin.validation.specsJson",
                    "Geçerli bir JSON değil.");
        }
    }

    private boolean checkVariantSku(String sku, Long excludeId, BindingResult bindingResult) {
        if (sku != null && !sku.isBlank()
                && productVariantRepository.existsBySkuAndIdNot(sku, excludeId)) {
            bindingResult.rejectValue("sku", "admin.validation.skuTaken", "Bu SKU kullanımda.");
            return false;
        }
        return true;
    }

    private static void copyVariant(VariantForm form, ProductVariant variant) {
        variant.setSku(form.sku());
        variant.setNameTr(form.nameTr());
        variant.setNameEn(nullIfBlank(form.nameEn()));
        variant.setPriceDelta(form.priceDelta());
        variant.setActive(Boolean.TRUE.equals(form.active()));
        variant.setSortOrder(form.sortOrder());
    }

    /** URL'deki ürün id'siyle sahipliği doğrulanmış varyant; uymazsa 404. */
    private ProductVariant ownedVariant(Long productId, Long variantId) {
        return productVariantRepository.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(productId))
                .orElseThrow(AdminCatalogService::notFound);
    }

    private ProductImage ownedImage(Long productId, Long imageId) {
        return productImageRepository.findById(imageId)
                .filter(i -> i.getProduct().getId().equals(productId))
                .orElseThrow(AdminCatalogService::notFound);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private static String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
