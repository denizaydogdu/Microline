package tr.com.microline.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductAndActiveTrueOrderBySortOrder(Product product);
}
