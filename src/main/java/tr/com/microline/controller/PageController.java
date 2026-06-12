package tr.com.microline.controller;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tr.com.microline.entity.CollectionCode;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.CollectionService;

/**
 * Statik içerik sayfaları. Metinler messages bundle'larından gelir; her
 * mapping model'e route koyar (dil değiştirici + Phase 8 hreflang bundan beslenir).
 */
@Controller
public class PageController {

    private final CollectionService collectionService;
    private final LocaleUrls localeUrls;

    public PageController(CollectionService collectionService, LocaleUrls localeUrls) {
        this.collectionService = collectionService;
        this.localeUrls = localeUrls;
    }

    @GetMapping({"/tr/hakkimizda", "/en/about"})
    public String about(Model model) {
        model.addAttribute("route", PageRoute.ABOUT);
        return "pages/about";
    }

    @GetMapping({"/tr/egitimciler", "/en/educators"})
    public String educators(Model model) {
        model.addAttribute("route", PageRoute.EDUCATORS);
        return "pages/educators";
    }

    @GetMapping({"/tr/urun-guvenligi", "/en/product-safety"})
    public String productSafety(Model model) {
        model.addAttribute("route", PageRoute.PRODUCT_SAFETY);
        return "pages/product-safety";
    }

    @GetMapping({"/tr/yesil-gelecek", "/en/greener-future"})
    public String sustainability(Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("route", PageRoute.SUSTAINABILITY);
        // "Mağazaya git" CTA'sı: kesim araçları koleksiyonu, seed yoksa ana sayfa
        model.addAttribute("shopUrl", collectionService.byCode(CollectionCode.CUTTING_TOOLS)
                .map(c -> localeUrls.collectionPath(c, locale))
                .orElse(PageRoute.HOME.path(locale)));
        return "pages/sustainability";
    }

    @GetMapping({"/tr/ortaklik-programi", "/en/affiliate-program"})
    public String affiliate(Model model) {
        model.addAttribute("route", PageRoute.AFFILIATE);
        return "pages/affiliate";
    }
}
