package tr.com.microline.admin.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tr.com.microline.admin.dto.LegalPageForm;
import tr.com.microline.admin.service.AdminContentService;
import tr.com.microline.entity.LegalPage;

/**
 * Yasal sayfa editörü: 9 sabit sayfa, yalnız başlık/gövde düzenlenir.
 * Create/delete yok; code ve slug'lar formda salt-okunur gösterilir.
 */
@Controller
@RequestMapping("/admin/yasal-sayfalar")
public class AdminLegalController {

    private final AdminContentService contentService;

    public AdminLegalController(AdminContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNav", "legal");
        model.addAttribute("legalPages", contentService.legalPages());
        return "admin/legal/list";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        LegalPage page = requirePage(id);
        model.addAttribute("adminNav", "legal");
        model.addAttribute("legalPage", page);
        if (!model.containsAttribute("legalPageForm")) {
            model.addAttribute("legalPageForm", toForm(page));
        }
        return "admin/legal/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute LegalPageForm legalPageForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        LegalPage page = requirePage(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminNav", "legal");
            model.addAttribute("legalPage", page);
            return "admin/legal/form";
        }
        contentService.updateLegalPage(id, legalPageForm);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        return "redirect:/admin/yasal-sayfalar/" + id;
    }

    private LegalPage requirePage(Long id) {
        return contentService.legalPage(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static LegalPageForm toForm(LegalPage page) {
        return new LegalPageForm(
                page.getTitleTr(),
                page.getTitleEn(),
                page.getBodyTr(),
                page.getBodyEn());
    }
}
