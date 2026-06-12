package tr.com.microline.admin.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
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
import tr.com.microline.admin.dto.ImageMetaForm;
import tr.com.microline.admin.dto.ProductForm;
import tr.com.microline.admin.dto.ProductImageForm;
import tr.com.microline.admin.dto.VariantForm;
import tr.com.microline.admin.service.AdminCatalogService;
import tr.com.microline.admin.service.ImageStorageException;
import tr.com.microline.entity.Product;

/**
 * Ürün CRUD + varyant ve görsel yöneticisi. Tüm mutasyonlar PRG + flash;
 * ana form ve düzenleme sayfasındaki EKLE formları hata durumunda alan
 * hatalarıyla yeniden render edilir, satır mini-formları (varyant
 * güncelleme, görsel meta) ise basitlik adına flashError + PRG kullanır.
 */
@Controller
@RequestMapping("/admin/urunler")
public class AdminProductController {

    private final AdminCatalogService catalogService;

    public AdminProductController(AdminCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /* ── Liste + create ──────────────────────────────────────────────── */

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNav", "products");
        model.addAttribute("products", catalogService.products());
        return "admin/products/list";
    }

    @GetMapping("/yeni")
    public String createForm(Model model) {
        model.addAttribute("adminNav", "products");
        model.addAttribute("collections", catalogService.collections());
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", emptyForm());
        }
        return "admin/products/form";
    }

    @PostMapping("/yeni")
    public String create(@Valid @ModelAttribute ProductForm productForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long savedId = catalogService.saveProduct(productForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminNav", "products");
            model.addAttribute("collections", catalogService.collections());
            return "admin/products/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.created");
        return "redirect:/admin/urunler/" + savedId;
    }

    /* ── Düzenleme ───────────────────────────────────────────────────── */

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = requireProduct(id);
        populateEditModel(product, model);
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", toForm(product));
        }
        return "admin/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ProductForm productForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Product product = requireProduct(id);
        // Form id'si URL'den gelir — gizli inputun oynanması yok sayılır
        ProductForm bound = withId(productForm, id);
        catalogService.saveProduct(bound, bindingResult);
        if (bindingResult.hasErrors()) {
            populateEditModel(product, model);
            return "admin/products/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/durum")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        catalogService.toggleProductActive(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/urunler/" + id;
    }

    /* ── Varyantlar ──────────────────────────────────────────────────── */

    @PostMapping("/{id}/varyantlar")
    public String addVariant(@PathVariable Long id,
                             @Valid @ModelAttribute VariantForm variantForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Product product = requireProduct(id);
        catalogService.addVariant(id, variantForm, bindingResult);
        if (bindingResult.hasErrors()) {
            populateEditModel(product, model);
            model.addAttribute("productForm", toForm(product));
            return "admin/products/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.created");
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/varyantlar/{vid}")
    public String updateVariant(@PathVariable Long id,
                                @PathVariable Long vid,
                                @Valid @ModelAttribute VariantForm variantForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        boolean saved = !bindingResult.hasErrors()
                && catalogService.updateVariant(id, vid, variantForm, bindingResult);
        if (!saved) {
            redirectAttributes.addFlashAttribute("flashError", flashErrorKey(bindingResult));
        } else {
            redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        }
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/varyantlar/{vid}/durum")
    public String toggleVariant(@PathVariable Long id,
                                @PathVariable Long vid,
                                RedirectAttributes redirectAttributes) {
        catalogService.toggleVariantActive(id, vid);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        return "redirect:/admin/urunler/" + id;
    }

    /* ── Görseller ───────────────────────────────────────────────────── */

    @PostMapping("/{id}/gorseller")
    public String uploadImage(@PathVariable Long id,
                              @Valid @ModelAttribute ProductImageForm productImageForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Product product = requireProduct(id);
        if (!bindingResult.hasErrors()) {
            try {
                catalogService.addImage(id, productImageForm);
            } catch (ImageStorageException e) {
                bindingResult.rejectValue("file", e.getMessageKey(), e.getMessage());
            }
        }
        if (bindingResult.hasErrors()) {
            populateEditModel(product, model);
            model.addAttribute("productForm", toForm(product));
            return "admin/products/form";
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.uploaded");
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/gorseller/{gid}")
    public String updateImageMeta(@PathVariable Long id,
                                  @PathVariable Long gid,
                                  @Valid @ModelAttribute ImageMetaForm imageMetaForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("flashError", flashErrorKey(bindingResult));
        } else {
            catalogService.updateImageMeta(id, gid, imageMetaForm);
            redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        }
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/gorseller/{gid}/kapak")
    public String setHero(@PathVariable Long id,
                          @PathVariable Long gid,
                          RedirectAttributes redirectAttributes) {
        catalogService.setHero(id, gid);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.saved");
        return "redirect:/admin/urunler/" + id;
    }

    @PostMapping("/{id}/gorseller/{gid}/sil")
    public String deleteImage(@PathVariable Long id,
                              @PathVariable Long gid,
                              RedirectAttributes redirectAttributes) {
        catalogService.deleteImage(id, gid);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.deleted");
        return "redirect:/admin/urunler/" + id;
    }

    /* ── Yardımcılar ─────────────────────────────────────────────────── */

    private Product requireProduct(Long id) {
        return catalogService.product(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Düzenleme sayfasının ürün-dışı yükü: koleksiyonlar + varyantlar + görseller. */
    private void populateEditModel(Product product, Model model) {
        model.addAttribute("adminNav", "products");
        model.addAttribute("product", product);
        model.addAttribute("collections", catalogService.collections());
        model.addAttribute("variants", catalogService.variants(product));
        model.addAttribute("images", catalogService.images(product));
        if (!model.containsAttribute("variantForm")) {
            model.addAttribute("variantForm", new VariantForm("", "", "", BigDecimal.ZERO, true, 0));
        }
        if (!model.containsAttribute("productImageForm")) {
            model.addAttribute("productImageForm", new ProductImageForm(null, "", ""));
        }
    }

    /**
     * Mini-form PRG hatası: alan hataları redirect'i atlatamaz, banner'da tek
     * anahtar gösterilir — SKU çakışması özel olarak ayrıştırılır, gerisi genel.
     */
    private static String flashErrorKey(BindingResult bindingResult) {
        boolean skuTaken = bindingResult.getFieldErrors("sku").stream()
                .anyMatch(e -> "admin.validation.skuTaken".equals(e.getCode()));
        return skuTaken ? "admin.validation.skuTaken" : "admin.flash.invalid";
    }

    private static ProductForm emptyForm() {
        return new ProductForm(null, "", "", "", "", "", "", "", "", "", "", "",
                "", null, null, null, false, true, 0, "", "");
    }

    private static ProductForm withId(ProductForm f, Long id) {
        return new ProductForm(id, f.sku(), f.slugTr(), f.slugEn(), f.nameTr(), f.nameEn(),
                f.taglineTr(), f.taglineEn(), f.descriptionTr(), f.descriptionEn(),
                f.safetyNotesTr(), f.safetyNotesEn(), f.specsJson(), f.priceAmount(),
                f.compareAtPrice(), f.collectionId(), f.featured(), f.active(),
                f.sortOrder(), f.metaDescriptionTr(), f.metaDescriptionEn());
    }

    private static ProductForm toForm(Product product) {
        return new ProductForm(
                product.getId(),
                product.getSku(),
                product.getSlugTr(),
                product.getSlugEn(),
                product.getNameTr(),
                product.getNameEn(),
                product.getTaglineTr(),
                product.getTaglineEn(),
                product.getDescriptionTr(),
                product.getDescriptionEn(),
                product.getSafetyNotesTr(),
                product.getSafetyNotesEn(),
                product.getSpecsJson(),
                product.getPriceAmount(),
                product.getCompareAtPrice(),
                product.getCollection().getId(),
                product.isFeatured(),
                product.isActive(),
                product.getSortOrder(),
                product.getMetaDescriptionTr(),
                product.getMetaDescriptionEn());
    }
}
