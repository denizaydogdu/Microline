package tr.com.microline.controller;

import java.time.Instant;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tr.com.microline.dto.ContactForm;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.InquiryService;

@Controller
public class ContactController {

    private final InquiryService inquiryService;

    public ContactController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping({"/tr/iletisim", "/en/contact"})
    public String form(Model model) {
        if (!model.containsAttribute("contactForm")) {
            model.addAttribute("contactForm",
                    new ContactForm(null, null, null, null, null, null, Instant.now().toEpochMilli()));
        }
        model.addAttribute("route", PageRoute.CONTACT);
        return "pages/contact";
    }

    @PostMapping({"/tr/iletisim", "/en/contact"})
    public String submit(@Valid @ModelAttribute("contactForm") ContactForm form,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("route", PageRoute.CONTACT);
            return "pages/contact";
        }
        Locale locale = LocaleContextHolder.getLocale();
        inquiryService.saveContactMessage(form, locale, request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("formSuccess", true);
        return "redirect:" + PageRoute.CONTACT.path(locale);
    }
}
