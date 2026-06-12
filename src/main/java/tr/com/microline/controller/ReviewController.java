package tr.com.microline.controller;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tr.com.microline.i18n.PageRoute;
import tr.com.microline.service.ReviewService;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping({"/tr/yorumlar", "/en/reviews"})
    public String reviews(Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("route", PageRoute.REVIEWS);
        model.addAttribute("stats", reviewService.stats(locale));
        model.addAttribute("featuredReviews", reviewService.featured(locale));
        model.addAttribute("reviews", reviewService.approved(locale));
        return "pages/reviews";
    }
}
