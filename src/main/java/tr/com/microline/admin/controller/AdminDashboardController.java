package tr.com.microline.admin.controller;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.entity.OrderRequest;
import tr.com.microline.repository.NewsletterSubscriberRepository;
import tr.com.microline.repository.OrderRequestRepository;
import tr.com.microline.repository.ProductRepository;

@Controller
public class AdminDashboardController {

    /** Instant şablonda doğrudan biçimlenemez (zone gerekir) — burada çözülür. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Europe/Istanbul"));

    /** Son talepler tablosunun satırı; statusKey ROOT locale ile üretilir (TR I→ı tuzağı). */
    public record RecentRequestRow(Long id, String fullName, String email, String createdAt, String statusKey) {
    }

    private final ProductRepository productRepository;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;
    private final OrderRequestRepository orderRequestRepository;

    public AdminDashboardController(ProductRepository productRepository,
                                    NewsletterSubscriberRepository newsletterSubscriberRepository,
                                    OrderRequestRepository orderRequestRepository) {
        this.productRepository = productRepository;
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
        this.orderRequestRepository = orderRequestRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("adminNav", "dashboard");
        // newRequestCount / newMessageCount AdminModelAdvice'tan gelir (sidebar da kullanır)
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("subscriberCount", newsletterSubscriberRepository.countByUnsubscribedAtIsNull());
        model.addAttribute("recentRequests", recentRequests());
        return "admin/dashboard";
    }

    private List<RecentRequestRow> recentRequests() {
        return orderRequestRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(this::toRow)
                .toList();
    }

    private RecentRequestRow toRow(OrderRequest request) {
        InquiryStatus status = request.getStatus();
        return new RecentRequestRow(
                request.getId(),
                request.getFullName(),
                request.getEmail(),
                DATE_FORMAT.format(request.getCreatedAt()),
                status.name().toLowerCase(Locale.ROOT));
    }
}
