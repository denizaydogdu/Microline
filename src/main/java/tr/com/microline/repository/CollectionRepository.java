package tr.com.microline.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.Collection;
import tr.com.microline.entity.CollectionCode;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByCode(CollectionCode code);

    Optional<Collection> findBySlugTr(String slugTr);

    Optional<Collection> findBySlugEn(String slugEn);

    List<Collection> findByActiveTrueOrderBySortOrder();

    /* --- Admin tarafı --- */

    List<Collection> findAllByOrderBySortOrderAscIdAsc();

    boolean existsBySlugTrAndIdNot(String slugTr, Long id);

    boolean existsBySlugEnAndIdNot(String slugEn, Long id);
}
