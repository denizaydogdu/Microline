package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlugTrAndActiveTrue(String slugTr);

    Optional<Product> findBySlugEnAndActiveTrue(String slugEn);

    List<Product> findByCollectionAndActiveTrueOrderBySortOrder(Collection collection);

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findByActiveTrueOrderBySortOrderAscIdAsc();
}
