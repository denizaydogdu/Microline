package tr.com.microline.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Admin listesi: product LAZY + open-in-view=false → render'da patlamasın diye graph. */
    @EntityGraph(attributePaths = "product")
    List<Review> findAllByOrderByCreatedAtDesc();

    /** Sidebar/dashboard "onay bekleyen" pill'i için (Phase E'de bağlanır). */
    long countByApprovedFalse();

    List<Review> findByApprovedTrueAndLocaleOrderByCreatedAtDesc(String locale);

    List<Review> findByProductAndApprovedTrueAndLocaleOrderByCreatedAtDesc(Product product, String locale);

    List<Review> findByFeaturedTrueAndApprovedTrueAndLocale(String locale);

    /** Dil-bağımsız özet istatistik için (dil örneklemi küçükken fallback). */
    List<Review> findByApprovedTrue();
}
