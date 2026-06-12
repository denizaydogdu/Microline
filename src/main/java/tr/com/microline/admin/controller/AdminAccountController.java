package tr.com.microline.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import tr.com.microline.admin.dto.PasswordChangeForm;
import tr.com.microline.admin.service.AdminAccountService;

@Controller
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/admin/sifre")
    public String passwordForm(Model model) {
        model.addAttribute("adminNav", "password");
        if (!model.containsAttribute("passwordChangeForm")) {
            model.addAttribute("passwordChangeForm", new PasswordChangeForm("", "", ""));
        }
        return "admin/password";
    }

    @PostMapping("/admin/sifre")
    public String changePassword(@Valid @ModelAttribute PasswordChangeForm passwordChangeForm,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 Model model) {
        if (!bindingResult.hasFieldErrors("newPasswordConfirm")
                && !passwordChangeForm.newPassword().equals(passwordChangeForm.newPasswordConfirm())) {
            bindingResult.rejectValue("newPasswordConfirm", "admin.password.mismatch",
                    "Şifreler eşleşmiyor.");
        }
        if (!bindingResult.hasErrors()
                && !adminAccountService.changePassword(authentication.getName(),
                        passwordChangeForm.currentPassword(), passwordChangeForm.newPassword())) {
            bindingResult.rejectValue("currentPassword", "admin.password.wrongCurrent",
                    "Mevcut şifre hatalı.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminNav", "password");
            return "admin/password";
        }
        // Şifre değişimi çoğunlukla "oturum ele geçirildi" şüphesiyle yapılır:
        // mevcut oturumu düşürüp yeni şifreyle yeniden girişe zorla
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/admin/giris?sifre-degisti";
    }
}
