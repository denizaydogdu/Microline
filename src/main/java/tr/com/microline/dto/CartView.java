package tr.com.microline.dto;

import java.math.BigDecimal;
import java.util.List;

import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;

/**
 * Sepetin render edilebilir hâli: satırlar DB'den çözülmüş, pasif ürünler
 * budanmış, toplamlar sunucuda hesaplanmış. removedAny=true ise kullanıcıya
 * "bazı ürünler kaldırıldı" uyarısı gösterilir.
 */
public record CartView(List<Line> lines, BigDecimal total, boolean removedAny) {

    public record Line(Product product, ProductVariant variant, int quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int totalUnits() {
        return lines.stream().mapToInt(Line::quantity).sum();
    }
}
