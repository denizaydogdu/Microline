package tr.com.microline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
