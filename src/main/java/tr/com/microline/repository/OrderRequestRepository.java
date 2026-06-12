package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.entity.OrderRequest;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    long countByStatus(InquiryStatus status);

    List<OrderRequest> findTop5ByOrderByCreatedAtDesc();

    Page<OrderRequest> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    Page<OrderRequest> findByStatusOrderByCreatedAtDescIdDesc(InquiryStatus status, Pageable pageable);

    /**
     * Talep detayı tek sorguda: items + product + variant LAZY ve
     * open-in-view=false — graph'sız detay render'ı LazyInitException verir.
     */
    @EntityGraph(attributePaths = {"items", "items.product", "items.variant"})
    Optional<OrderRequest> findWithItemsById(Long id);
}
