package tr.com.microline.controller;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import tr.com.microline.entity.LegalPage;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.service.LegalPageService;

/**
 * Yasal sayfaların fallback rotası: /tr/mesafeli-satis-sozlesmesi gibi üst
 * seviye slug'lar buradan servis edilir. Literal pattern'lar (örn. /tr/yorumlar)
 * Spring'in pattern özgüllüğünde her zaman önce eşleşir; bu mapping yalnızca
 * hiçbir literal rotaya uymayan iki-segmentli yolları yakalar.
 */
@Controller
public class LegalPageController {

    private final LegalPageService legalPageService;
    private final LocaleUrls localeUrls;

    public LegalPageController(LegalPageService legalPageService, LocaleUrls localeUrls) {
        this.legalPageService = legalPageService;
        this.localeUrls = localeUrls;
    }

    @GetMapping("/{lang:tr|en}/{slug}")
    public String legalPage(@PathVariable String lang, @PathVariable String slug, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        LegalPage page = legalPageService.bySlug(slug, locale)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("page", page);
        model.addAttribute("alternateUrl", legalPath(page, localeUrls.other(locale)));
        // İngilizce gövde henüz çevrilmediyse Türkçe gösterilir; şablon uyarı bandı basar
        model.addAttribute("untranslated",
                Locale.ENGLISH.getLanguage().equals(locale.getLanguage())
                        && (page.getBodyEn() == null || page.getBodyEn().isBlank()));
        return "pages/legal";
    }

    private String legalPath(LegalPage page, Locale locale) {
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) && page.getSlugEn() != null) {
            return "/en/" + page.getSlugEn();
        }
        return "/tr/" + page.getSlugTr();
    }
}
