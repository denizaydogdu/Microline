package tr.com.microline.dto;

/**
 * Sepet ekleme/güncelleme/kaldırma POST'ları. Bean Validation yok:
 * eksik productId controller'da elenir, adet servis tarafında 1-999'a
 * clamp'lenir (kaldırma quantity'yi yok sayar).
 */
public record CartAddForm(Long productId, Long variantId, Integer quantity) {

    public int quantityOrDefault() {
        return quantity == null ? 1 : quantity;
    }
}
