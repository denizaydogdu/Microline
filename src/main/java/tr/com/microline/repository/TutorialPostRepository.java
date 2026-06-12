package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.TutorialPost;

public interface TutorialPostRepository extends JpaRepository<TutorialPost, Long> {

    List<TutorialPost> findByPublishedTrueOrderByPublishedAtDesc();

    /** Admin listesi taslakları da kapsar; publishedAt null olabildiğinden createdAt'e sıralanır. */
    List<TutorialPost> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsBySlugTrAndIdNot(String slugTr, Long id);

    boolean existsBySlugEnAndIdNot(String slugEn, Long id);

    Optional<TutorialPost> findBySlugTrAndPublishedTrue(String slugTr);

    Optional<TutorialPost> findBySlugEnAndPublishedTrue(String slugEn);
}
