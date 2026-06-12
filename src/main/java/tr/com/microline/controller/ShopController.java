package tr.com.microline.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import tr.com.microline.entity.Collection;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.Review;
import tr.com.microline.i18n.LocaleUrls;
import tr.com.microline.service.CollectionService;
import tr.com.microline.service.ProductService;

/**
 * Dinamik mağaza sayfaları. PageRoute statik sayfalar içindir; burada dil
 * değiştirici, model'e konan açık "alternateUrl" ile beslenir (header
 * fragment'ı alternateUrl varsa onu, yoksa route'u kullanır).
 */
@Controller
public class ShopController {

    private final CollectionService collectionService;
    private final ProductService productService;
    private final LocaleUrls localeUrls;

    public ShopController(CollectionService collectionService,
                          ProductService productService,
                          LocaleUrls localeUrls) {
        this.collectionService = collectionService;
        this.productService = productService;
        this.localeUrls = localeUrls;
    }

    @GetMapping({"/tr/koleksiyon/{slug}", "/en/collections/{slug}"})
    public String collection(@PathVariable String slug, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        Collection collection = collectionService.bySlug(slug, locale)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("collection", collection);
        model.addAttribute("products", productService.byCollection(collection));
        model.addAttribute("alternateUrl", localeUrls.collectionPath(collection, localeUrls.other(locale)));
        return "pages/collection";
    }

    @GetMapping({"/tr/urun/{slug}", "/en/products/{slug}"})
    public String product(@PathVariable String slug, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        Product product = productService.bySlug(slug, locale)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Review> reviews = productService.approvedReviews(product, locale);
        double ratingAvg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        model.addAttribute("product", product);
        model.addAttribute("images", productService.images(product));
        model.addAttribute("variants", productService.variants(product));
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("ratingAvg", ratingAvg);
        model.addAttribute("ratingRounded", (int) Math.round(ratingAvg));
        model.addAttribute("specs", productService.specs(product, locale));
        model.addAttribute("alternateUrl", localeUrls.productPath(product, localeUrls.other(locale)));
        return "pages/product";
    }
}
