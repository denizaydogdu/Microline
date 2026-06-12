package tr.com.microline.admin.controller;

import jakarta.validation.Valid;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import tr.com.microline.admin.dto.TutorialForm;
import tr.com.microline.admin.service.AdminContentService;
import tr.com.microline.admin.service.ImageStorageException;
import tr.com.microline.entity.Difficulty;
import tr.com.microline.entity.TutorialPost;

/**
 * Eğitim CRUD + yayın toggle'ı. Tüm mutasyonlar PRG + flash; ana form hata
 * durumunda alan hatalarıyla yeniden render edilir. Eğitim HARD silinebilir
 * (FK'siz) — silme butonu data-confirm ile korunur.
 */
@Controller
@RequestMapping("/admin/egitimler")
public class AdminTutorialController {

    /** Instant şablonda doğrudan biçimlenemez (zone gerekir) — burada çözülür. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Istanbul"));

    /** Liste satırı; dateText taslakta null kalır (şablon — basar). */
    public record TutorialRow(Long id, String titleTr, Difficulty difficulty,
                              boolean published, String dateText) {
    }

    private final AdminContentService contentService;

    public AdminTutorialController(AdminContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNav", "tutorials");
        model.addAttribute("tutorials", rows());
        return "admin/tutorials/list";
    }

    @GetMapping("/yeni")
    public String createForm(Model model) {
        populateFormModel(null, model);
        if (!model.containsAttribute("tutorialForm")) {
            model.addAttribute("tutorialForm", emptyForm());
        }
        return "admin/tutorials/form";
    }

    @PostMapping("/yeni")
    public String create(@Valid @ModelAttribute TutorialForm tutorialForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long savedId = save(tutorialForm, bindingResult);
        if (bindingResult.hasErrors()) {
            populateFormModel(null, model);
            return "admin/tutorials/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.created");
        return "redirect:/admin/egitimler/" + savedId;
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        TutorialPost post = requireTutorial(id);
        populateFormModel(post, model);
        if (!model.containsAttribute("tutorialForm")) {
            model.addAttribute("tutorialForm", toForm(post));
        }
        return "admin/tutorials/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute TutorialForm tutorialForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        TutorialPost post = requireTutorial(id);
        // Form id'si URL'den gelir — gizli inputun oynanması yok sayılır
        save(withId(tutorialForm, id), bindingResult);
        if (bindingResult.hasErrors()) {
            populateFormModel(post, model);
            return "admin/tutorials/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        return "redirect:/admin/egitimler/" + id;
    }

    @PostMapping("/{id}/durum")
    public String togglePublished(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contentService.togglePublished(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/egitimler";
    }

    @PostMapping("/{id}/sil")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contentService.deleteTutorial(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.deleted");
        return "redirect:/admin/egitimler";
    }

    /* ── Yardımcılar ─────────────────────────────────────────────────── */

    /** Kapak depolama hatasını alan hatasına çevirerek kaydeder. */
    private Long save(TutorialForm form, BindingResult bindingResult) {
        try {
            return contentService.saveTutorial(form, bindingResult);
        } catch (ImageStorageException e) {
            bindingResult.rejectValue("coverImage", e.getMessageKey(), e.getMessage());
            return null;
        }
    }

    private TutorialPost requireTutorial(Long id) {
        return contentService.tutorial(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void populateFormModel(TutorialPost post, Model model) {
        model.addAttribute("adminNav", "tutorials");
        model.addAttribute("tutorial", post);
        // T() expression yerine model attr: şablon enum sınıfına bağımlı kalmaz
        model.addAttribute("difficulties", Difficulty.values());
    }

    private List<TutorialRow> rows() {
        return contentService.tutorials().stream()
                .map(t -> new TutorialRow(t.getId(), t.getTitleTr(), t.getDifficulty(),
                        t.isPublished(),
                        t.getPublishedAt() != null ? DATE_FORMAT.format(t.getPublishedAt()) : null))
                .toList();
    }

    private static TutorialForm emptyForm() {
        return new TutorialForm(null, "", "", "", "", "", "", "", "", "",
                Difficulty.BEGINNER, false, null);
    }

    private static TutorialForm withId(TutorialForm f, Long id) {
        return new TutorialForm(id, f.slugTr(), f.slugEn(), f.titleTr(), f.titleEn(),
                f.excerptTr(), f.excerptEn(), f.bodyTr(), f.bodyEn(), f.videoUrl(),
                f.difficulty(), f.published(), f.coverImage());
    }

    private static TutorialForm toForm(TutorialPost post) {
        return new TutorialForm(
                post.getId(),
                post.getSlugTr(),
                post.getSlugEn(),
                post.getTitleTr(),
                post.getTitleEn(),
                post.getExcerptTr(),
                post.getExcerptEn(),
                post.getBodyTr(),
                post.getBodyEn(),
                post.getVideoUrl(),
                post.getDifficulty(),
                post.isPublished(),
                null);
    }
}
