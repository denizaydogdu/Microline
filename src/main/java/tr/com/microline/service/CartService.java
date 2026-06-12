package tr.com.microline.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.dto.CartView;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;

/**
 * Session sepeti üzerinde doğrulamalı işlemler. Cart bean'i session-scoped
 * proxy olarak enjekte edilir; bu servis singleton kalır.
 */
@Service
public class CartService {

    /** Oturum belleğini sınırlamak için ayrı satır üst sınırı. */
    private static final int MAX_LINES = 20;

    private final Cart cart;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartService(Cart cart,
                       ProductRepository productRepository,
                       ProductVariantRepository productVariantRepository) {
        this.cart = cart;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Doğrulayıp ekler: pasif/bilinmeyen ürün, ürüne ait olmayan varyant veya
     * satır limiti aşımı sessizce reddedilir (tamper'lı isteğe sinyal verilmez).
     * Eklenen ürünün entity'si döner — controller PRG hedefini (ürün sayfası)
     * buradan kurar.
     */
    @Transactional(readOnly = true)
    public Product add(long productId, Long variantId, int quantity) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElse(null);
        if (product == null) {
            return null;
        }
        Long validVariantId = resolveVariantId(product, variantId);
        if (variantId != null && validVariantId == null) {
            return null;
        }
        boolean newLine = !cart.containsLine(productId, validVariantId);
        if (newLine && cart.distinctLineCount() >= MAX_LINES) {
            return null;
        }
        cart.merge(productId, validVariantId, clamp(quantity));
        return product;
    }

    public void updateQuantity(long productId, Long variantId, int quantity) {
        cart.setQuantity(productId, variantId, clamp(quantity));
    }

    public void remove(long productId, Long variantId) {
        cart.remove(productId, variantId);
    }

    public void clear() {
        cart.clear();
    }

    public List<Cart.CartLine> snapshotLines() {
        return cart.snapshot();
    }

    public int totalUnits() {
        return cart.totalUnits();
    }

    /**
     * Satırları DB'den çözer; bu arada pasifleşen ürün/varyant satırları
     * sepetten BUDANIR (removedAny=true → şablon uyarı bandı basar).
     * Ölü satırı checkout'a taşımaktansa bir kerelik uyarı tercih edilir.
     */
    @Transactional(readOnly = true)
    public CartView view(Locale locale) {
        List<CartView.Line> lines = new ArrayList<>();
        List<Cart.LineKey> dead = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart.CartLine line : cart.snapshot()) {
            Product product = productRepository.findById(line.productId())
                    .filter(Product::isActive)
                    .orElse(null);
            if (product == null) {
                dead.add(new Cart.LineKey(line.productId(), line.variantId()));
                continue;
            }
            ProductVariant variant = null;
            if (line.variantId() != null) {
                variant = productVariantRepository.findById(line.variantId())
                        .filter(v -> v.isActive() && v.getProduct().getId().equals(product.getId()))
                        .orElse(null);
                if (variant == null) {
                    dead.add(new Cart.LineKey(line.productId(), line.variantId()));
                    continue;
                }
            }
            BigDecimal unitPrice = unitPrice(product, variant);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.quantity()));
            lines.add(new CartView.Line(product, variant, line.quantity(), unitPrice, lineTotal));
            total = total.add(lineTotal);
        }

        if (!dead.isEmpty()) {
            cart.removeKeys(dead);
        }
        return new CartView(lines, total, !dead.isEmpty());
    }

    static BigDecimal unitPrice(Product product, ProductVariant variant) {
        BigDecimal base = product.getPriceAmount();
        return variant == null ? base : base.add(variant.getPriceDelta());
    }

    private Long resolveVariantId(Product product, Long variantId) {
        if (variantId == null) {
            return null;
        }
        return productVariantRepository.findById(variantId)
                .filter(v -> v.isActive() && v.getProduct().getId().equals(product.getId()))
                .map(ProductVariant::getId)
                .orElse(null);
    }

    private int clamp(int quantity) {
        return Math.max(1, Math.min(999, quantity));
    }
}
