package tr.com.microline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.OrderRequest;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
}
