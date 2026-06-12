package tr.com.microline.i18n;

import java.util.Locale;

/**
 * Statik sayfaların merkezi rota tablosu (CLAUDE.md). Dil değiştirici,
 * hreflang ve sitemap bu enum'dan beslenir; yeni statik sayfa = yeni değer.
 */
public enum PageRoute {

    HOME("", ""),
    REVIEWS("yorumlar", "reviews"),
    TUTORIALS("egitimler", "tutorials"),
    EDUCATORS("egitimciler", "educators"),
    ABOUT("hakkimizda", "about"),
    PRODUCT_SAFETY("urun-guvenligi", "product-safety"),
    SUSTAINABILITY("yesil-gelecek", "greener-future"),
    AFFILIATE("ortaklik-programi", "affiliate-program"),
    CONTACT("iletisim", "contact"),
    CART("sepet", "cart"),
    ORDER_REQUEST("siparis-talebi", "order-request");

    private final String trSlug;
    private final String enSlug;

    PageRoute(String trSlug, String enSlug) {
        this.trSlug = trSlug;
        this.enSlug = enSlug;
    }

    public String trSlug() {
        return trSlug;
    }

    public String enSlug() {
        return enSlug;
    }

    /** Ana sayfa "/tr/" ve "/en/" (sondaki slash kanonik), diğerleri "/tr/{slug}". */
    public String path(Locale locale) {
        boolean english = Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        String prefix = english ? "/en/" : "/tr/";
        String slug = english ? enSlug : trSlug;
        return slug.isEmpty() ? prefix : prefix + slug;
    }
}
