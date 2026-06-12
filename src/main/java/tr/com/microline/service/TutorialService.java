package tr.com.microline.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.entity.TutorialPost;
import tr.com.microline.repository.TutorialPostRepository;

@Service
@Transactional(readOnly = true)
public class TutorialService {

    private final TutorialPostRepository tutorialPostRepository;

    public TutorialService(TutorialPostRepository tutorialPostRepository) {
        this.tutorialPostRepository = tutorialPostRepository;
    }

    public List<TutorialPost> published() {
        return tutorialPostRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    /** Slug, mevcut dilin kolonunda aranır (/tr → slug_tr, /en → slug_en). */
    public Optional<TutorialPost> bySlug(String slug, Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage())
                ? tutorialPostRepository.findBySlugEnAndPublishedTrue(slug)
                : tutorialPostRepository.findBySlugTrAndPublishedTrue(slug);
    }
}
