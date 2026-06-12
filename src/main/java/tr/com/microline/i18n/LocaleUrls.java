package tr.com.microline.i18n;

import java.util.Locale;

import org.springframework.stereotype.Component;

import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.TutorialPost;

/**
 * Şablonlardan ${@localeUrls...} ile erişilen yardımcı; dil değiştirici
 * aynı sayfanın diğer dildeki URL'ini buradan alır. Dinamik sayfaların
 * (ürün/koleksiyon) dil-bazlı slug yolları da burada merkezîleşir.
 */
@Component("localeUrls")
public class LocaleUrls {

    public String alternate(PageRoute route, Locale current) {
        return route.path(other(current));
    }

    public Locale other(Locale current) {
        return Locale.ENGLISH.getLanguage().equals(current.getLanguage())
                ? PathLocaleResolver.TURKISH
                : Locale.ENGLISH;
    }

    /** EN slug yoksa Türkçe sayfaya düşer (Türkçe-öncelikli fallback kuralı). */
    public String collectionPath(Collection collection, Locale locale) {
        if (isEnglish(locale) && collection.getSlugEn() != null) {
            return "/en/collections/" + collection.getSlugEn();
        }
        return "/tr/koleksiyon/" + collection.getSlugTr();
    }

    public String productPath(Product product, Locale locale) {
        if (isEnglish(locale) && product.getSlugEn() != null) {
            return "/en/products/" + product.getSlugEn();
        }
        return "/tr/urun/" + product.getSlugTr();
    }

    public String tutorialPath(TutorialPost post, Locale locale) {
        if (isEnglish(locale) && post.getSlugEn() != null) {
            return "/en/tutorials/" + post.getSlugEn();
        }
        return "/tr/egitimler/" + post.getSlugTr();
    }

    private boolean isEnglish(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
    }
}
