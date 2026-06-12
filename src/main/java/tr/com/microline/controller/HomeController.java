package tr.com.microline.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tr.com.microline.entity.CollectionCode;
import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.CollectionService;
import tr.com.microline.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CollectionService collectionService;

    public HomeController(ProductService productService, CollectionService collectionService) {
        this.productService = productService;
        this.collectionService = collectionService;
    }

    @GetMapping({"/tr/", "/en/"})
    public String home(Model model) {
        model.addAttribute("route", PageRoute.HOME);
        // Seed boşsa (taze DB) bölümler th:if ile gizlenir — null güvenli.
        model.addAttribute("flagship", productService.flagship().orElse(null));
        model.addAttribute("featuredProducts", productService.featuredFirst(4));
        model.addAttribute("cuttingTools",
                collectionService.byCode(CollectionCode.CUTTING_TOOLS).orElse(null));
        model.addAttribute("bundles",
                collectionService.byCode(CollectionCode.BUNDLES).orElse(null));
        return "pages/home";
    }

    @GetMapping({"/", "/tr"})
    public String redirectToTurkishHome() {
        return "redirect:/tr/";
    }

    @GetMapping("/en")
    public String redirectToEnglishHome() {
        return "redirect:/en/";
    }
}
