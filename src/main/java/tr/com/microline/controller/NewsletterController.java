package tr.com.microline.controller;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tr.com.microline.dto.NewsletterForm;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.InquiryService;

/**
 * Bülten formu hangi sayfaya gömüldüyse oraya geri döner; hedef gizli
 * "redirect" alanından gelir ve open-redirect'e karşı yalnızca site-içi
 * /tr|/en yollarına izin verilir.
 */
@Controller
public class NewsletterController {

    private final InquiryService inquiryService;

    public NewsletterController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping({"/tr/bulten-abonelik", "/en/newsletter"})
    public String subscribe(@Valid @ModelAttribute("newsletterForm") NewsletterForm form,
                            BindingResult bindingResult,
                            @RequestParam(name = "redirect", required = false) String redirect,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("newsletterError", true);
        } else {
            inquiryService.subscribeNewsletter(form, locale, request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("newsletterSuccess", true);
        }
        return "redirect:" + safeTarget(redirect, locale);
    }

    private String safeTarget(String redirect, Locale locale) {
        String fallback = PageRoute.HOME.path(locale);
        if (redirect == null || redirect.isBlank()) {
            return fallback;
        }
        // Tarayıcılar \ karakterini / olarak normalize eder; aynı normalizasyonla
        // doğrula. URI parser yerine düz string kontrolü: parser'ların kenar
        // durumları (authority/path ayrımı) bu doğrulamada sürpriz üretir.
        String candidate = redirect.replace('\\', '/');
        boolean siteRelative = (candidate.startsWith("/tr/") || candidate.startsWith("/en/"))
                && !candidate.contains("//")     // protokol-göreli kaçışlar
                && !candidate.contains("..")     // path traversal
                && !candidate.contains(":");     // scheme kaçakları
        return siteRelative ? candidate : fallback;
    }
}
