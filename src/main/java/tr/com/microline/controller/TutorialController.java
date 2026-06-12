package tr.com.microline.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import tr.com.microline.entity.TutorialPost;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.ProductService;
import tr.com.microline.service.TutorialService;

/**
 * Eğitim hub'ı + detay. Hub statik PageRoute ile çalışır; detay sayfası dil
 * değiştiriciyi ShopController kalıbındaki açık alternateUrl ile besler.
 * Hub mapping'i literal, detay parameterized — Spring özgüllük sırası çakışmayı önler.
 */
@Controller
public class TutorialController {

    /** publishedAt UTC Instant'tır; okuyucuya yerel (TR) takvim günü gösterilir. */
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Istanbul");

    private final TutorialService tutorialService;
    private final ProductService productService;
    private final LocaleUrls localeUrls;

    public TutorialController(TutorialService tutorialService,
                              ProductService productService,
                              LocaleUrls localeUrls) {
        this.tutorialService = tutorialService;
        this.productService = productService;
        this.localeUrls = localeUrls;
    }

    @GetMapping({"/tr/egitimler", "/en/tutorials"})
    public String hub(Model model) {
        model.addAttribute("route", PageRoute.TUTORIALS);
        model.addAttribute("tutorials", tutorialService.published());
        return "pages/tutorials";
    }

    @GetMapping({"/tr/egitimler/{slug}", "/en/tutorials/{slug}"})
    public String detail(@PathVariable String slug, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        TutorialPost post = tutorialService.bySlug(slug, locale)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("post", post);
        model.addAttribute("publishedDate", formatPublishedAt(post.getPublishedAt(), locale));
        model.addAttribute("alternateUrl", localeUrls.tutorialPath(post, localeUrls.other(locale)));
        // CTA şeridi amiral gemisi ürüne gider; seed boşsa şerit gizlenir (th:if)
        model.addAttribute("ctaUrl", productService.flagship()
                .map(p -> localeUrls.productPath(p, locale))
                .orElse(null));
        return "pages/tutorial-detail";
    }

    private String formatPublishedAt(Instant publishedAt, Locale locale) {
        if (publishedAt == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
                .format(publishedAt.atZone(DISPLAY_ZONE));
    }
}
