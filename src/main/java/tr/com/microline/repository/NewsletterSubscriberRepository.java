package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.NewsletterSubscriber;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmail(String email);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(UUID unsubscribeToken);

    /** Aktif (çıkış yapmamış) abone sayısı — admin paneli istatistiği. */
    long countByUnsubscribedAtIsNull();

    Page<NewsletterSubscriber> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** CSV dışa aktarımı sayfalamasızdır — tüm liste tek seferde yazılır. */
    List<NewsletterSubscriber> findAllByOrderByCreatedAtDesc();
}
