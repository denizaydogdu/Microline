package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.TutorialPost;

public interface TutorialPostRepository extends JpaRepository<TutorialPost, Long> {

    List<TutorialPost> findByPublishedTrueOrderByPublishedAtDesc();

    Optional<TutorialPost> findBySlugTrAndPublishedTrue(String slugTr);

    Optional<TutorialPost> findBySlugEnAndPublishedTrue(String slugEn);
}
