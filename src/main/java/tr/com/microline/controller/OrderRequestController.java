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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tr.com.microline.dto.CartView;
import tr.com.microline.dto.CheckoutForm;
import tr.com.microline.entity.Product;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.i18n.Prices;
import tr.com.microline.service.CartService;
import tr.com.microline.service.InquiryService;
import tr.com.microline.service.ProductService;

/**
 * Checkout: sepet özeti (salt-okunur) + iletişim formu. Sipariş satırları
 * formdan değil session sepetinden gelir. PRG akışı; spam sonucu da başarı
 * gibi gösterilir ve sepet her iki durumda da temizlenir (sinyal sızmasın).
 */
@Controller
public class OrderRequestController {

    private final CartService cartService;
    private final InquiryService inquiryService;
    private final ProductService productService;
    private final LocaleUrls localeUrls;
    private final Prices prices;

    public OrderRequestController(CartService cartService,
                                  InquiryService inquiryService,
                                  ProductService productService,
                                  LocaleUrls localeUrls,
                                  Prices prices) {
        this.cartService = cartService;
        this.inquiryService = inquiryService;
        this.productService = productService;
        this.localeUrls = localeUrls;
        this.prices = prices;
    }

    @GetMapping({"/tr/siparis-talebi", "/en/order-request"})
    public String form(@RequestParam(name = "urun", required = false) String urunSlug,
                       @RequestParam(name = "product", required = false) String productSlug,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();

        // Eski tek-ürün derin linkleri (?urun=slug): niyet "bu ürünü istiyorum" —
        // GET'le sepete eklemek mutasyon olurdu; ürün sayfasına yönlendirilir.
        String legacySlug = urunSlug != null ? urunSlug : productSlug;
        if (legacySlug != null) {
            Product product = productService.bySlug(legacySlug, locale).orElse(null);
            if (product != null) {
                return "redirect:" + localeUrls.productPath(product, locale);
            }
        }

        CartView cart = cartService.view(locale);
        boolean justSubmitted = model.containsAttribute("formSuccess");
        if (cart.isEmpty() && !justSubmitted) {
            redirectAttributes.addFlashAttribute("cartEmptyCheckout", true);
            return "redirect:" + PageRoute.CART.path(locale);
        }

        if (!model.containsAttribute("checkoutForm")) {
            model.addAttribute("checkoutForm",
                    new CheckoutForm(null, null, null, null, null, null, Instant.now().toEpochMilli()));
        }
        model.addAttribute("route", PageRoute.ORDER_REQUEST);
        model.addAttribute("cart", cart);
        return "pages/order-request";
    }

    @PostMapping({"/tr/siparis-talebi", "/en/order-request"})
    public String submit(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        CartView cart = cartService.view(locale);

        if (bindingResult.hasErrors()) {
            model.addAttribute("route", PageRoute.ORDER_REQUEST);
            model.addAttribute("cart", cart);
            return "pages/order-request";
        }

        InquiryService.Result result = inquiryService.saveOrderRequest(
                form, cartService.snapshotLines(), locale, request.getRemoteAddr());

        if (result == InquiryService.Result.EMPTY_CART) {
            redirectAttributes.addFlashAttribute("cartEmptyCheckout", true);
            return "redirect:" + PageRoute.CART.path(locale);
        }

        // Başarı banner'ı, sepet temizlenmeden önceki özet ekosunu flash'tan alır
        redirectAttributes.addFlashAttribute("formSuccess", true);
        redirectAttributes.addFlashAttribute("orderedCount", cart.totalUnits());
        redirectAttributes.addFlashAttribute("orderedTotal", prices.format(cart.total(), locale));
        cartService.clear();
        return "redirect:" + PageRoute.ORDER_REQUEST.path(locale);
    }
}
