package tr.com.microline.admin.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Yalnız giriş sayfasını render eder; POST /admin/giris ve POST /admin/cikis
 * Spring Security'nin formLogin/logout filtrelerince işlenir (controller yok).
 */
@Controller
public class AdminAuthController {

    @GetMapping("/admin/giris")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/admin";
        }
        return "admin/login";
    }
}
