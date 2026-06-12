package tr.com.microline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.OrderRequestItem;

public interface OrderRequestItemRepository extends JpaRepository<OrderRequestItem, Long> {
}
