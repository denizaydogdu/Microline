package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.LegalPage;

public interface LegalPageRepository extends JpaRepository<LegalPage, Long> {

    /** 9 sabit sayfa; seed sırası (id) anlamlı gruplamayı korur. */
    List<LegalPage> findAllByOrderByIdAsc();

    Optional<LegalPage> findBySlugTr(String slugTr);

    Optional<LegalPage> findBySlugEn(String slugEn);
}
