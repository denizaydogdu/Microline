package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlugTrAndActiveTrue(String slugTr);

    Optional<Product> findBySlugEnAndActiveTrue(String slugEn);

    List<Product> findByCollectionAndActiveTrueOrderBySortOrder(Collection collection);

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findByActiveTrueOrderBySortOrderAscIdAsc();

    /* --- Admin tarafı: pasifler dahil; koleksiyon adı listede basıldığından
           lazy collection burada fetch edilir (open-in-view=false) --- */

    @EntityGraph(attributePaths = "collection")
    List<Product> findAllByOrderBySortOrderAscIdAsc();

    /** Benzersizlik: create'te henüz id yoktur, çağıran -1 geçer (hiçbir satırla eşleşmez). */
    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsBySlugTrAndIdNot(String slugTr, Long id);

    boolean existsBySlugEnAndIdNot(String slugEn, Long id);
}
