package tr.com.microline.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.NewsletterSubscriber;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmail(String email);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(UUID unsubscribeToken);
}
