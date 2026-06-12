package tr.com.microline.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Oturuma bağlı sepet: yalnızca ID + adet tutar (entity yok — fiyat/ad her
 * render'da DB'den çözülür, bayatlama imkânsız). Salt veri taşıyıcıdır;
 * doğrulama ve DB erişimi CartService'tedir. Mutasyonlar synchronized:
 * aynı oturumun iki sekmesi aynı bean'e POST atabilir.
 */
@Component
@SessionScope
public class Cart implements Serializable {

    /** Satır anahtarı: aynı (ürün, varyant) ikilisi tek satırda birleşir. */
    public record LineKey(long productId, Long variantId) implements Serializable {
    }

    public record CartLine(long productId, Long variantId, int quantity) implements Serializable {
    }

    private final Map<LineKey, Integer> lines = new LinkedHashMap<>();

    public synchronized void merge(long productId, Long variantId, int quantity) {
        lines.merge(new LineKey(productId, variantId), quantity,
                (oldQty, addQty) -> Math.min(999, oldQty + addQty));
    }

    public synchronized void setQuantity(long productId, Long variantId, int quantity) {
        LineKey key = new LineKey(productId, variantId);
        if (lines.containsKey(key)) {
            lines.put(key, quantity);
        }
    }

    public synchronized void remove(long productId, Long variantId) {
        lines.remove(new LineKey(productId, variantId));
    }

    public synchronized void removeKeys(List<LineKey> keys) {
        keys.forEach(lines::remove);
    }

    public synchronized void clear() {
        lines.clear();
    }

    public synchronized boolean isEmpty() {
        return lines.isEmpty();
    }

    public synchronized int distinctLineCount() {
        return lines.size();
    }

    public synchronized boolean containsLine(long productId, Long variantId) {
        return lines.containsKey(new LineKey(productId, variantId));
    }

    public synchronized int totalUnits() {
        return lines.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Anlık kopya: servis/controller iterasyon sırasında mutasyondan etkilenmez. */
    public synchronized List<CartLine> snapshot() {
        List<CartLine> result = new ArrayList<>(lines.size());
        lines.forEach((key, qty) -> result.add(new CartLine(key.productId(), key.variantId(), qty)));
        return result;
    }

    @Override
    public synchronized boolean equals(Object o) {
        return this == o || (o instanceof Cart other && lines.equals(other.lines));
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(lines);
    }
}
