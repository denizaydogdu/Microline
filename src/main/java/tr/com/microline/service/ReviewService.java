package tr.com.microline.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.dto.ReviewStats;
import tr.com.microline.entity.Review;
import tr.com.microline.repository.ReviewRepository;

/**
 * Yorumlar sayfası okumaları. Yorumlar tek dilli orijinaldir: listeler daima
 * mevcut dile göre filtrelenir; özet istatistik ise dil örneklemi küçükken
 * tüm onaylı yorumlardan hesaplanır (tek dilde 1-2 yorumla yanıltıcı ortalama
 * göstermemek için).
 */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private static final int MIN_LOCALE_SAMPLE = 3;

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> approved(Locale locale) {
        return reviewRepository.findByApprovedTrueAndLocaleOrderByCreatedAtDesc(lang(locale));
    }

    public List<Review> featured(Locale locale) {
        return reviewRepository.findByFeaturedTrueAndApprovedTrueAndLocale(lang(locale));
    }

    public ReviewStats stats(Locale locale) {
        List<Review> sample = approved(locale);
        if (sample.size() < MIN_LOCALE_SAMPLE) {
            sample = reviewRepository.findByApprovedTrue();
        }
        if (sample.isEmpty()) {
            return ReviewStats.EMPTY;
        }
        double average = sample.stream().mapToInt(Review::getRating).average().orElse(0);
        long fiveStars = sample.stream().filter(r -> r.getRating() == 5).count();
        int fiveStarPercent = (int) Math.round(100.0 * fiveStars / sample.size());
        return new ReviewStats(average, sample.size(), fiveStarPercent);
    }

    private String lang(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? "en" : "tr";
    }
}
