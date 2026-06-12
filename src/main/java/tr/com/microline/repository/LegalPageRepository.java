package tr.com.microline.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.LegalPage;

public interface LegalPageRepository extends JpaRepository<LegalPage, Long> {

    Optional<LegalPage> findBySlugTr(String slugTr);

    Optional<LegalPage> findBySlugEn(String slugEn);
}
