package tr.com.microline.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductAndActiveTrueOrderBySortOrder(Product product);

    /* --- Admin tarafı: pasif varyantlar da listelenir --- */

    List<ProductVariant> findByProductOrderBySortOrderAscIdAsc(Product product);

    /** Varyant SKU'su tüm varyantlar arasında benzersizdir; create'te id yerine -1 geçilir. */
    boolean existsBySkuAndIdNot(String sku, Long id);
}
