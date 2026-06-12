package tr.com.microline.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import tr.com.microline.entity.Collection;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.Cart;
import tr.com.microline.service.CollectionService;

/**
 * Her modele dil bağlamını ekler. Controller'lar kendi sayfaları için
 * model'e "route" (PageRoute) koyar; dinamik sayfalar (ürün/koleksiyon)
 * bunun yerine açık "alternateUrl" koyar — header fragment'ı önce onu arar.
 * Mega-menü koleksiyonları da burada eklenir; header her sayfada (hata
 * sayfaları dahil) render edildiği için advice seviyesinde olmak zorunda.
 *
 * Kapsam BİLEREK daraltılmıştır: yalnız public controller paketi + Boot'un
 * ErrorController'ı (hata şablonları header'ı navCollections/routes ile
 * render eder). Admin controller'ları (tr.com.microline.admin.*) hariçtir —
 * onlara AdminModelAdvice bakar.
 */
@ControllerAdvice(basePackageClasses = HomeController.class, assignableTypes = ErrorController.class)
public class GlobalModelAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAdvice.class);

    private final CollectionService collectionService;
    private final Cart cart;
    private final String baseUrl;

    public GlobalModelAdvice(CollectionService collectionService, Cart cart,
                             @Value("${microline.base-url}") String baseUrl) {
        this.collectionService = collectionService;
        this.cart = cart;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @ModelAttribute
    public void addGlobals(Model model, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String currentLang = Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? "en" : "tr";
        model.addAttribute("currentLang", currentLang);
        model.addAttribute("baseUrl", baseUrl);
        // Canonical, query string'siz mevcut yol üzerinden kurulur (?urun= vb. varyantlar tekilleşir)
        model.addAttribute("canonicalUrl", baseUrl + request.getRequestURI());

        Map<String, PageRoute> routes = new LinkedHashMap<>();
        Arrays.stream(PageRoute.values()).forEach(route -> routes.put(route.name(), route));
        model.addAttribute("routes", routes);

        model.addAttribute("navCollections", navCollections());
        model.addAttribute("cartCount", cartCount());
    }

    /** Header rozeti; hata sayfası dahil her render'da çağrılır — asla fırlatmasın. */
    private int cartCount() {
        try {
            return cart.totalUnits();
        } catch (RuntimeException e) {
            // Session-scoped proxy, request dışı bağlamda (nadir) çözülemeyebilir
            return 0;
        }
    }

    /**
     * Hata sayfası da bu advice'tan geçer: 500'ün sebebi veritabanıysa bu
     * sorgu tekrar fırlayıp hata sayfasını da düşürür. Boş menü, sayfasız
     * kalmaktan iyidir.
     */
    private List<Collection> navCollections() {
        try {
            return collectionService.activeCollections();
        } catch (RuntimeException e) {
            log.warn("Mega-menü koleksiyonları yüklenemedi: {}", e.getMessage());
            return List.of();
        }
    }
}
