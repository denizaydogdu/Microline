package tr.com.microline.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByApprovedTrueAndLocaleOrderByCreatedAtDesc(String locale);

    List<Review> findByProductAndApprovedTrueAndLocaleOrderByCreatedAtDesc(Product product, String locale);

    List<Review> findByFeaturedTrueAndApprovedTrueAndLocale(String locale);

    /** Dil-bağımsız özet istatistik için (dil örneklemi küçükken fallback). */
    List<Review> findByApprovedTrue();
}
