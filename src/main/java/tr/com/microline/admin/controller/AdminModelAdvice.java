package tr.com.microline.admin.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.repository.ContactMessageRepository;
import tr.com.microline.repository.OrderRequestRepository;

/**
 * Admin controller'larının (bu paket) karşılığı GlobalModelAdvice değil
 * budur: topbar kullanıcı adı + sidebar'daki NEW sayaç pill'leri.
 * Public tarafın navCollections/cart yükü admin'e hiç girmez.
 */
@ControllerAdvice(basePackageClasses = AdminDashboardController.class)
public class AdminModelAdvice {

    private final OrderRequestRepository orderRequestRepository;
    private final ContactMessageRepository contactMessageRepository;

    public AdminModelAdvice(OrderRequestRepository orderRequestRepository,
                            ContactMessageRepository contactMessageRepository) {
        this.orderRequestRepository = orderRequestRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @ModelAttribute
    public void addAdminGlobals(Model model, Authentication authentication) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("adminUserName", authenticated ? authentication.getName() : null);
        // Sayaç sorguları yalnız kimlik doğrulanmışsa: giriş sayfası (sidebar
        // render etmez) gereksiz DB yükü almasın
        model.addAttribute("newRequestCount",
                authenticated ? orderRequestRepository.countByStatus(InquiryStatus.NEW) : 0L);
        model.addAttribute("newMessageCount",
                authenticated ? contactMessageRepository.countByStatus(InquiryStatus.NEW) : 0L);
    }
}
