package tr.com.microline.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.i18n.PathLocaleResolver;
import tr.com.microline.repository.CollectionRepository;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.TutorialPostRepository;

/**
 * sitemap.xml içeriği: PageRoute (CART ve ORDER_REQUEST hariç — işlemsel,
 * noindex sayfalar) + aktif ürün/koleksiyon ve yayınlanmış eğitim slug'ları.
 * Her URL'e xhtml:link tr/en alternate'leri eklenir (hreflang eşleri).
 * Yasal sayfalar noindex olduğundan sitemap'e girmez.
 */
@Service
public class SitemapService {

    private final String baseUrl;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final TutorialPostRepository tutorialPostRepository;
    private final LocaleUrls localeUrls;

    public SitemapService(@Value("${microline.base-url}") String baseUrl,
                          CollectionRepository collectionRepository,
                          ProductRepository productRepository,
                          TutorialPostRepository tutorialPostRepository,
                          LocaleUrls localeUrls) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.collectionRepository = collectionRepository;
        this.productRepository = productRepository;
        this.tutorialPostRepository = tutorialPostRepository;
        this.localeUrls = localeUrls;
    }

    @Transactional(readOnly = true)
    public String buildXml() {
        // LinkedHashMap<trPath, enPath> — null enPath = yalnız TR girdisi
        Map<String, String> pairs = new LinkedHashMap<>();

        for (PageRoute route : PageRoute.values()) {
            if (route == PageRoute.CART || route == PageRoute.ORDER_REQUEST) {
                continue;
            }
            pairs.put(route.path(PathLocaleResolver.TURKISH), route.path(Locale.ENGLISH));
        }
        collectionRepository.findByActiveTrueOrderBySortOrder().forEach(c ->
                pairs.put(localeUrls.collectionPath(c, PathLocaleResolver.TURKISH),
                          localeUrls.collectionPath(c, Locale.ENGLISH)));
        productRepository.findByActiveTrueOrderBySortOrderAscIdAsc().forEach(p ->
                pairs.put(localeUrls.productPath(p, PathLocaleResolver.TURKISH),
                          localeUrls.productPath(p, Locale.ENGLISH)));
        tutorialPostRepository.findByPublishedTrueOrderByPublishedAtDesc().forEach(t ->
                pairs.put(localeUrls.tutorialPath(t, PathLocaleResolver.TURKISH),
                          localeUrls.tutorialPath(t, Locale.ENGLISH)));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"")
           .append(" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");
        pairs.forEach((tr, en) -> {
            appendUrl(xml, tr, tr, en);
            if (en != null && !en.equals(tr)) {
                appendUrl(xml, en, tr, en);
            }
        });
        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void appendUrl(StringBuilder xml, String loc, String trPath, String enPath) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(baseUrl).append(loc).append("</loc>\n");
        xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"tr\" href=\"")
           .append(baseUrl).append(trPath).append("\"/>\n");
        if (enPath != null) {
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"en\" href=\"")
               .append(baseUrl).append(enPath).append("\"/>\n");
        }
        xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
           .append(baseUrl).append(trPath).append("\"/>\n");
        xml.append("  </url>\n");
    }

    public String baseUrl() {
        return baseUrl;
    }
}
