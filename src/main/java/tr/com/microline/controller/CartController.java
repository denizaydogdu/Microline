package tr.com.microline.controller;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tr.com.microline.dto.CartAddForm;
import tr.com.microline.entity.CollectionCode;
import tr.com.microline.entity.Product;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.CartService;
import tr.com.microline.service.CollectionService;

/**
 * Sepet sayfası + mutasyon uçları. Tüm mutasyonlar CSRF'li POST'tur — GET
 * asla sepeti değiştirmez (prefetcher/crawler sepet dolduramaz). Bean
 * Validation yerine null kontrolü + servis tarafında clamp yeterlidir;
 * geçersiz/tamper'lı istekler sessizce sepete yönlendirilir.
 */
@Controller
public class CartController {

    private final CartService cartService;
    private final LocaleUrls localeUrls;
    private final CollectionService collectionService;

    public CartController(CartService cartService, LocaleUrls localeUrls,
                          CollectionService collectionService) {
        this.cartService = cartService;
        this.localeUrls = localeUrls;
        this.collectionService = collectionService;
    }

    @GetMapping({"/tr/sepet", "/en/cart"})
    public String cart(Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        var view = cartService.view(locale);
        model.addAttribute("route", PageRoute.CART);
        model.addAttribute("cart", view);
        // Boş sepet kurtarması ana sayfaya değil mağazaya gitsin (1 adım az)
        model.addAttribute("shopUrl", collectionService.byCode(CollectionCode.CUTTING_TOOLS)
                .map(c -> localeUrls.collectionPath(c, locale))
                .orElse(PageRoute.HOME.path(locale)));
        if (view.removedAny()) {
            model.addAttribute("cartItemsRemoved", true);
        }
        return "pages/cart";
    }

    /** Başarıda ürün sayfasına döner (#cart-banner) — alışveriş bağlamı korunur. */
    @PostMapping({"/tr/sepet/ekle", "/en/cart/add"})
    public String add(@ModelAttribute("cartAddForm") CartAddForm form,
                      RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (form.productId() == null) {
            return "redirect:" + PageRoute.CART.path(locale);
        }
        Product added = cartService.add(form.productId(), form.variantId(), form.quantityOrDefault());
        if (added == null) {
            return "redirect:" + PageRoute.CART.path(locale);
        }
        redirectAttributes.addFlashAttribute("cartAdded", true);
        return "redirect:" + localeUrls.productPath(added, locale) + "#cart-banner";
    }

    @PostMapping({"/tr/sepet/guncelle", "/en/cart/update"})
    public String update(@ModelAttribute("cartAddForm") CartAddForm form,
                         RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (form.productId() != null) {
            int requested = form.quantityOrDefault();
            cartService.updateQuantity(form.productId(), form.variantId(), requested);
            if (requested < 1 || requested > 999) {
                redirectAttributes.addFlashAttribute("cartQtyAdjusted", true);
            }
        }
        return "redirect:" + PageRoute.CART.path(locale);
    }

    @PostMapping({"/tr/sepet/kaldir", "/en/cart/remove"})
    public String remove(@ModelAttribute("cartAddForm") CartAddForm form) {
        Locale locale = LocaleContextHolder.getLocale();
        if (form.productId() != null) {
            cartService.remove(form.productId(), form.variantId());
        }
        return "redirect:" + PageRoute.CART.path(locale);
    }
}
