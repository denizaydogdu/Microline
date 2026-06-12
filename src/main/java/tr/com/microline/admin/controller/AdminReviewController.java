package tr.com.microline.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tr.com.microline.admin.service.AdminContentService;

/**
 * Yorum moderasyonu: ayrı form sayfası yok, liste üzerinde toggle/sil
 * mini-formları. Onay/öne çıkarma adanmış endpoint'ler olduğundan satır
 * formlarında hidden input taşımaya gerek yoktur (varyant kalıbının aksine).
 */
@Controller
@RequestMapping("/admin/yorumlar")
public class AdminReviewController {

    private final AdminContentService contentService;

    public AdminReviewController(AdminContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNav", "reviews");
        model.addAttribute("reviews", contentService.reviews());
        return "admin/reviews/list";
    }

    @PostMapping("/{id}/onay")
    public String toggleApproved(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contentService.toggleApproved(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/yorumlar";
    }

    @PostMapping("/{id}/one-cikar")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contentService.toggleFeatured(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/yorumlar";
    }

    @PostMapping("/{id}/sil")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contentService.deleteReview(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.deleted");
        return "redirect:/admin/yorumlar";
    }
}
