package tr.com.microline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.ContactMessage;
import tr.com.microline.entity.InquiryStatus;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    long countByStatus(InquiryStatus status);

    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(InquiryStatus status, Pageable pageable);
}
