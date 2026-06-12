package tr.com.microline.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductOrderBySortOrder(Product product);
}
