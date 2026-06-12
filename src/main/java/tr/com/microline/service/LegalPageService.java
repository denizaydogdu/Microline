package tr.com.microline.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.entity.LegalPage;
import tr.com.microline.repository.LegalPageRepository;

@Service
@Transactional(readOnly = true)
public class LegalPageService {

    private final LegalPageRepository legalPageRepository;

    public LegalPageService(LegalPageRepository legalPageRepository) {
        this.legalPageRepository = legalPageRepository;
    }

    public Optional<LegalPage> bySlug(String slug, Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage())
                ? legalPageRepository.findBySlugEn(slug)
                : legalPageRepository.findBySlugTr(slug);
    }
}
