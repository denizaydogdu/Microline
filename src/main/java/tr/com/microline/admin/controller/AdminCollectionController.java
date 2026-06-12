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
import tr.com.microline.admin.dto.CollectionForm;
import tr.com.microline.admin.service.AdminCatalogService;
import tr.com.microline.admin.service.ImageStorageException;
import tr.com.microline.entity.Collection;

/**
 * Koleksiyon yönetimi: yalnız düzenleme + aktiflik toggle'ı. Create/delete
 * YOK — 4 koleksiyon CollectionCode enum'una sabitlenmiştir.
 */
@Controller
@RequestMapping("/admin/koleksiyonlar")
public class AdminCollectionController {

    private final AdminCatalogService catalogService;

    public AdminCollectionController(AdminCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNav", "collections");
        model.addAttribute("collections", catalogService.collections());
        return "admin/collections/list";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Collection collection = catalogService.collection(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("adminNav", "collections");
        model.addAttribute("collection", collection);
        if (!model.containsAttribute("collectionForm")) {
            model.addAttribute("collectionForm", toForm(collection));
        }
        return "admin/collections/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CollectionForm collectionForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Collection collection = catalogService.collection(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            catalogService.updateCollection(id, collectionForm, bindingResult);
        } catch (ImageStorageException e) {
            bindingResult.rejectValue("heroImage", e.getMessageKey(), e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminNav", "collections");
            model.addAttribute("collection", collection);
            return "admin/collections/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        return "redirect:/admin/koleksiyonlar/" + id;
    }

    @PostMapping("/{id}/durum")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        catalogService.toggleCollectionActive(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/koleksiyonlar";
    }

    private static CollectionForm toForm(Collection collection) {
        return new CollectionForm(
                collection.getId(),
                collection.getSlugTr(),
                collection.getSlugEn(),
                collection.getNameTr(),
                collection.getNameEn(),
                collection.getDescriptionTr(),
                collection.getDescriptionEn(),
                collection.getSortOrder(),
                collection.isActive(),
                null);
    }
}
