package tr.com.microline.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.entity.Collection;
import tr.com.microline.entity.CollectionCode;
import tr.com.microline.repository.CollectionRepository;

/**
 * open-in-view=false olduğu için tüm okuma işlemleri burada, transaction
 * içinde tamamlanır; şablonlara lazy ilişki sızdırılmaz.
 */
@Service
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    /** Mega-menü ve koleksiyon listeleri için: aktifler, sort_order sırasıyla. */
    public List<Collection> activeCollections() {
        return collectionRepository.findByActiveTrueOrderBySortOrder();
    }

    /** Slug, mevcut dilin kolonunda aranır (/tr → slug_tr, /en → slug_en). */
    public Optional<Collection> bySlug(String slug, Locale locale) {
        Optional<Collection> found = Locale.ENGLISH.getLanguage().equals(locale.getLanguage())
                ? collectionRepository.findBySlugEn(slug)
                : collectionRepository.findBySlugTr(slug);
        return found.filter(Collection::isActive);
    }

    public Optional<Collection> byCode(CollectionCode code) {
        return collectionRepository.findByCode(code).filter(Collection::isActive);
    }
}
