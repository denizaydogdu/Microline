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

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductImage;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.repository.CollectionRepository;
import tr.com.microline.repository.ProductImageRepository;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;
import tr.com.microline.service.ProductService;

/**
 * Katalog yönetimi uçtan uca. Test DB'si context'ler arasında paylaşıldığı
 * için her mutasyon testi tohum verisini sonunda eski haline döndürür —
 * sonraki test sınıfları (sepet/SEO) seed fiyat ve görsellerine güvenir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class AdminCatalogTests {

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
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductService productService;

    /** UserDetailsService'e dokunmadan ROLE_ADMIN kimliği basar. */
    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    private Product flagship() {
        return productRepository.findBySlugTrAndActiveTrue("microline-kesim-makinesi").orElseThrow();
    }

    private Long anyCollectionId() {
        return collectionRepository.findAll().getFirst().getId();
    }

    /** Tüm zorunlu alanları dolu, slug'ı BOŞ bırakılmış geçerli create yükü. */
    private MultiValueMap<String, String> validProductParams(String sku, String nameTr) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sku", sku);
        params.add("slugTr", "");
        params.add("slugEn", "");
        params.add("nameTr", nameTr);
        params.add("taglineTr", "Test sloganı");
        params.add("descriptionTr", "<p>Test açıklaması</p>");
        params.add("metaDescriptionTr", "Test meta açıklaması");
        params.add("priceAmount", "199.90");
        params.add("collectionId", String.valueOf(anyCollectionId()));
        params.add("sortOrder", "10");
        params.add("active", "true");
        return params;
    }

    /** Update formu da TAM yük ister — mevcut üründen kopyalanır. */
    private MultiValueMap<String, String> paramsFrom(Product p) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sku", p.getSku());
        params.add("slugTr", p.getSlugTr());
        params.add("slugEn", p.getSlugEn() == null ? "" : p.getSlugEn());
        params.add("nameTr", p.getNameTr());
        params.add("nameEn", p.getNameEn() == null ? "" : p.getNameEn());
        params.add("taglineTr", p.getTaglineTr());
        params.add("descriptionTr", p.getDescriptionTr());
        params.add("safetyNotesTr", p.getSafetyNotesTr());
        params.add("specsJson", p.getSpecsJson() == null ? "" : p.getSpecsJson());
        params.add("metaDescriptionTr", p.getMetaDescriptionTr());
        params.add("priceAmount", p.getPriceAmount().toPlainString());
        params.add("compareAtPrice", p.getCompareAtPrice() == null ? "" : p.getCompareAtPrice().toPlainString());
        params.add("collectionId", String.valueOf(p.getCollection().getId()));
        params.add("sortOrder", String.valueOf(p.getSortOrder()));
        params.add("featured", String.valueOf(p.isFeatured()));
        params.add("active", String.valueOf(p.isActive()));
        return params;
    }

    private MockHttpServletRequestBuilder adminPost(String urlTemplate, Object... vars) {
        return post(urlTemplate, vars).with(admin()).with(csrf());
    }

    /* ── Liste + create ─────────────────────────────────────────────── */

    /** Thymeleaf hataları ancak render'da patlar — tüm GET sayfaları açılır. */
    @Test
    void catalogPagesRender() throws Exception {
        Collection collection = collectionRepository.findBySlugTr("kesim-araclari").orElseThrow();
        mockMvc.perform(get("/admin/urunler/yeni").with(admin()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/urunler/{id}", flagship().getId()).with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Varyantlar")));
        mockMvc.perform(get("/admin/koleksiyonlar").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Kesim Araçları")));
        mockMvc.perform(get("/admin/koleksiyonlar/{id}", collection.getId()).with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void productListShowsFlagship() throws Exception {
        mockMvc.perform(get("/admin/urunler").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Microline Kesim Makinesi")));
    }

    @Test
    void createWithBlankSlugGeneratesTurkishSafeSlug() throws Exception {
        mockMvc.perform(adminPost("/admin/urunler/yeni")
                        .params(validProductParams("ML-T1", "Yeni Test Şablonu")))
                .andExpect(status().is3xxRedirection());

        // ı/ş → i/s: Türkçe harita + Locale.ROOT lowercase kanıtı
        Product created = productRepository.findBySlugTrAndActiveTrue("yeni-test-sablonu").orElseThrow();
        assertThat(created.getSku()).isEqualTo("ML-T1");
        // nameEn boş bırakıldı → slugEn türetilmez, NULL kalır
        assertThat(created.getSlugEn()).isNull();
    }

    @Test
    void duplicateSlugReRendersFormWithoutSaving() throws Exception {
        MultiValueMap<String, String> params = validProductParams("ML-DUP1", "Kopya Slug Denemesi");
        params.set("slugTr", "microline-kesim-makinesi");

        mockMvc.perform(adminPost("/admin/urunler/yeni").params(params))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productForm", "slugTr"));

        assertThat(productRepository.existsBySkuAndIdNot("ML-DUP1", -1L)).isFalse();
    }

    @Test
    void duplicateSkuRejected() throws Exception {
        mockMvc.perform(adminPost("/admin/urunler/yeni")
                        .params(validProductParams("ML-C1", "Kopya SKU Denemesi")))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productForm", "sku"));
    }

    @Test
    void invalidSpecsJsonRejected() throws Exception {
        MultiValueMap<String, String> params = validProductParams("ML-JSON1", "Bozuk JSON Denemesi");
        params.set("specsJson", "not json");

        mockMvc.perform(adminPost("/admin/urunler/yeni").params(params))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productForm", "specsJson"));

        assertThat(productRepository.existsBySkuAndIdNot("ML-JSON1", -1L)).isFalse();
    }

    /* ── Düzenleme + durum ──────────────────────────────────────────── */

    @Test
    void editPersistsNewPrice() throws Exception {
        Product flagship = flagship();
        BigDecimal originalPrice = flagship.getPriceAmount();
        MultiValueMap<String, String> params = paramsFrom(flagship);
        params.set("priceAmount", "5490.00");

        mockMvc.perform(adminPost("/admin/urunler/{id}", flagship.getId()).params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/urunler/*"));

        Product reloaded = productRepository.findById(flagship.getId()).orElseThrow();
        assertThat(reloaded.getPriceAmount()).isEqualByComparingTo("5490.00");

        // Seed fiyatına dönülür: sepet/SEO testleri bu değere güvenir
        reloaded.setPriceAmount(originalPrice);
        productRepository.save(reloaded);
    }

    @Test
    void toggleRemovesFromPublicCatalogAndBack() throws Exception {
        Product flagship = flagship();
        int activeBefore = productService.activeProducts().size();

        mockMvc.perform(adminPost("/admin/urunler/{id}/durum", flagship.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(productService.activeProducts()).hasSize(activeBefore - 1);

        mockMvc.perform(adminPost("/admin/urunler/{id}/durum", flagship.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(productService.activeProducts()).hasSize(activeBefore);
    }

    /* ── Varyantlar ─────────────────────────────────────────────────── */

    @Test
    void addVariantWithNegativeDelta() throws Exception {
        Product flagship = flagship();

        mockMvc.perform(adminPost("/admin/urunler/{id}/varyantlar", flagship.getId())
                        .param("sku", "ML-C1-ECO")
                        .param("nameTr", "Eko Paket")
                        .param("priceDelta", "-250.00")
                        .param("active", "true")
                        .param("sortOrder", "5"))
                .andExpect(status().is3xxRedirection());

        ProductVariant variant = productVariantRepository.findByProductOrderBySortOrderAscIdAsc(flagship)
                .stream().filter(v -> "ML-C1-ECO".equals(v.getSku())).findFirst().orElseThrow();
        assertThat(variant.getPriceDelta()).isEqualByComparingTo("-250.00");
    }

    @Test
    void duplicateVariantSkuRejected() throws Exception {
        Product flagship = flagship();
        long variantsBefore = productVariantRepository.count();

        // ML-C1-STD seed varyantı — global varyant SKU benzersizliği
        mockMvc.perform(adminPost("/admin/urunler/{id}/varyantlar", flagship.getId())
                        .param("sku", "ML-C1-STD")
                        .param("nameTr", "Kopya Varyant")
                        .param("priceDelta", "0.00")
                        .param("active", "true")
                        .param("sortOrder", "9"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("variantForm", "sku"));

        assertThat(productVariantRepository.count()).isEqualTo(variantsBefore);
    }

    /* ── Görsel yöneticisi (tek yaşam döngüsü: sıra bağımsızlığı için) ── */

    @Test
    void imageLifecycleUploadMetaHeroDelete() throws Exception {
        Product product = productRepository.findBySlugTrAndActiveTrue("prokesim-bicak-modulu").orElseThrow();
        String originalHero = product.getHeroImageUrl();

        // Yükle
        mockMvc.perform(multipart("/admin/urunler/{id}/gorseller", product.getId())
                        .file(new MockMultipartFile("file", "yeni görsel.jpeg", "image/jpeg", JPEG_BYTES))
                        .param("altTr", "Test alternatif metni")
                        .with(admin()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        List<ProductImage> images = productImageRepository.findByProductOrderBySortOrder(product);
        ProductImage uploaded = images.stream()
                .filter(i -> i.getUrl().startsWith("/uploads/product/"))
                .findFirst().orElseThrow();
        assertThat(uploaded.getAltTr()).isEqualTo("Test alternatif metni");
        Path stored = UPLOADS_ROOT.resolve(uploaded.getUrl().substring("/uploads/".length()));
        assertThat(stored).exists();

        // Meta güncelle
        mockMvc.perform(adminPost("/admin/urunler/{id}/gorseller/{gid}", product.getId(), uploaded.getId())
                        .param("altTr", "Güncellenmiş alt metin")
                        .param("altEn", "Updated alt text")
                        .param("sortOrder", "7"))
                .andExpect(status().is3xxRedirection());
        ProductImage updated = productImageRepository.findById(uploaded.getId()).orElseThrow();
        assertThat(updated.getAltTr()).isEqualTo("Güncellenmiş alt metin");
        assertThat(updated.getSortOrder()).isEqualTo(7);

        // Kapak yap
        mockMvc.perform(adminPost("/admin/urunler/{id}/gorseller/{gid}/kapak", product.getId(), uploaded.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(productRepository.findById(product.getId()).orElseThrow().getHeroImageUrl())
                .isEqualTo(uploaded.getUrl());

        // Sil: satır + dosya birlikte gider
        mockMvc.perform(adminPost("/admin/urunler/{id}/gorseller/{gid}/sil", product.getId(), uploaded.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(productImageRepository.findById(uploaded.getId())).isEmpty();
        assertThat(Files.notExists(stored)).isTrue();

        // Kapak seed görseline döndürülür (silinen dosyaya işaret etmesin)
        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        reloaded.setHeroImageUrl(originalHero);
        productRepository.save(reloaded);
    }

    /* ── Koleksiyonlar ──────────────────────────────────────────────── */

    @Test
    void collectionEditPersistsName() throws Exception {
        Collection collection = collectionRepository.findBySlugTr("kesim-araclari").orElseThrow();
        String originalName = collection.getNameTr();

        mockMvc.perform(multipart("/admin/koleksiyonlar/{id}", collection.getId())
                        .param("slugTr", collection.getSlugTr())
                        .param("slugEn", collection.getSlugEn())
                        .param("nameTr", "Kesim Araçları (Düzenlendi)")
                        .param("descriptionTr", collection.getDescriptionTr())
                        .param("sortOrder", String.valueOf(collection.getSortOrder()))
                        .param("active", "true")
                        .with(admin()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        Collection reloaded = collectionRepository.findById(collection.getId()).orElseThrow();
        assertThat(reloaded.getNameTr()).isEqualTo("Kesim Araçları (Düzenlendi)");

        reloaded.setNameTr(originalName);
        collectionRepository.save(reloaded);
    }

    @Test
    void collectionHeroUploadStoresUnderCollectionFolder() throws Exception {
        Collection collection = collectionRepository.findBySlugTr("eklentiler").orElseThrow();
        String originalHero = collection.getHeroImageUrl();

        mockMvc.perform(multipart("/admin/koleksiyonlar/{id}", collection.getId())
                        .file(new MockMultipartFile("heroImage", "kapak.jpg", "image/jpeg", JPEG_BYTES))
                        .param("slugTr", collection.getSlugTr())
                        .param("slugEn", collection.getSlugEn())
                        .param("nameTr", collection.getNameTr())
                        .param("descriptionTr", collection.getDescriptionTr())
                        .param("sortOrder", String.valueOf(collection.getSortOrder()))
                        .param("active", "true")
                        .with(admin()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        Collection reloaded = collectionRepository.findById(collection.getId()).orElseThrow();
        assertThat(reloaded.getHeroImageUrl()).startsWith("/uploads/collection/");
        assertThat(UPLOADS_ROOT.resolve(reloaded.getHeroImageUrl().substring("/uploads/".length())))
                .exists();

        // Seed görseline dönüş (eski /img/... URL'i deleteIfUploaded'a takılmaz)
        reloaded.setHeroImageUrl(originalHero);
        collectionRepository.save(reloaded);
    }

    /* ── Zincir regresyonu ──────────────────────────────────────────── */

    @Test
    void anonymousCreateRedirectsToAdminLogin() throws Exception {
        mockMvc.perform(post("/admin/urunler/yeni").with(csrf())
                        .params(validProductParams("ML-ANON1", "Anonim Deneme")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/giris"));

        assertThat(productRepository.existsBySkuAndIdNot("ML-ANON1", -1L)).isFalse();
    }
}
