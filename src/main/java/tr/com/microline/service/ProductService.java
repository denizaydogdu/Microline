package tr.com.microline.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductImage;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.entity.Review;
import tr.com.microline.repository.ProductImageRepository;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;
import tr.com.microline.repository.ReviewRepository;

/**
 * open-in-view=false: görsel/varyant/yorum listeleri lazy ilişki üzerinden
 * değil, ayrı sorgularla çekilir — şablonlar detached entity'lerle çalışır.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          ProductVariantRepository productVariantRepository,
                          ReviewRepository reviewRepository,
                          ObjectMapper objectMapper,
                          MessageSource messageSource) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.reviewRepository = reviewRepository;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    /** Slug, mevcut dilin kolonunda aranır (/tr → slug_tr, /en → slug_en). */
    public Optional<Product> bySlug(String slug, Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage())
                ? productRepository.findBySlugEnAndActiveTrue(slug)
                : productRepository.findBySlugTrAndActiveTrue(slug);
    }

    public List<Product> byCollection(Collection collection) {
        return productRepository.findByCollectionAndActiveTrueOrderBySortOrder(collection);
    }

    /** Form POST'unda id ile doğrulama: pasif ürün seçilemez. */
    public Optional<Product> byId(Long id) {
        return productRepository.findById(id).filter(Product::isActive);
    }

    public List<Product> activeProducts() {
        return productRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
    }

    /** Ana sayfa hero'sunun bağlandığı amiral gemisi: ilk featured ürün. */
    public Optional<Product> flagship() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream().findFirst();
    }

    /** Önce featured'lar, kalan yer diğer aktif ürünlerle dolar (ana sayfa grid'i). */
    public List<Product> featuredFirst(int limit) {
        List<Product> result = new ArrayList<>(productRepository.findByFeaturedTrueAndActiveTrue());
        for (Product candidate : productRepository.findByActiveTrueOrderBySortOrderAscIdAsc()) {
            if (result.size() >= limit) {
                break;
            }
            if (result.stream().noneMatch(p -> p.getId().equals(candidate.getId()))) {
                result.add(candidate);
            }
        }
        return result.size() > limit ? result.subList(0, limit) : result;
    }

    public List<ProductImage> images(Product product) {
        return productImageRepository.findByProductOrderBySortOrder(product);
    }

    public List<ProductVariant> variants(Product product) {
        return productVariantRepository.findByProductAndActiveTrueOrderBySortOrder(product);
    }

    /** Yorumlar tek dilli orijinaller: yalnızca onaylı + mevcut dildekiler. */
    public List<Review> approvedReviews(Product product, Locale locale) {
        String lang = Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? "en" : "tr";
        return reviewRepository.findByProductAndApprovedTrueAndLocaleOrderByCreatedAtDesc(product, lang);
    }

    /**
     * specsJson'ı şablonun düz satır olarak basabileceği görüntü map'ine açar.
     * Değer dönüşümleri: liste → ", " ile birleşik, boolean → common.yes/no.
     * Bozuk JSON sayfayı düşürmez; boş tablo render edilir.
     */
    public Map<String, String> specs(Product product, Locale locale) {
        String json = product.getSpecsJson();
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        Map<String, Object> raw;
        try {
            raw = objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (JsonProcessingException e) {
            log.warn("specs_json çözümlenemedi (sku={}): {}", product.getSku(), e.getMessage());
            return Map.of();
        }
        Map<String, String> display = new LinkedHashMap<>();
        raw.forEach((key, value) -> display.put(key, displayValue(value, locale)));
        return display;
    }

    private String displayValue(Object value, Locale locale) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        if (value instanceof Boolean bool) {
            return messageSource.getMessage(bool ? "common.yes" : "common.no", null, locale);
        }
        return String.valueOf(value);
    }
}
