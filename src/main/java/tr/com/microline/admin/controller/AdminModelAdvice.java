package tr.com.microline.admin.controller;

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
        // Giriş sayfası da bu advice'tan geçer — authentication null olabilir
        model.addAttribute("adminUserName", authentication != null ? authentication.getName() : null);
        model.addAttribute("newRequestCount", orderRequestRepository.countByStatus(InquiryStatus.NEW));
        model.addAttribute("newMessageCount", contactMessageRepository.countByStatus(InquiryStatus.NEW));
    }
}
