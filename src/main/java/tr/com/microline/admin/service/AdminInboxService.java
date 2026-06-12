package tr.com.microline.admin.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tr.com.microline.entity.ContactMessage;
import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.entity.NewsletterSubscriber;
import tr.com.microline.entity.OrderRequest;
import tr.com.microline.entity.OrderRequestItem;
import tr.com.microline.repository.ContactMessageRepository;
import tr.com.microline.repository.NewsletterSubscriberRepository;
import tr.com.microline.repository.OrderRequestRepository;

/**
 * Gelen kutusu okuma/durum tarafı (sipariş talepleri, iletişim mesajları,
 * bülten aboneleri). Talep/mesaj içerikleri ziyaretçi verisidir — burada
 * yalnız durum alanı yazılır, içerik asla düzenlenmez/silinmez (KVKK kayıt
 * bütünlüğü). Sayfalama yalnız bu listelerde vardır (20/sayfa, controller
 * belirler). Durumlar arası geçiş serbesttir (NEW/CONTACTED/CLOSED).
 */
@Service
public class AdminInboxService {

    /**
     * Talep detayının render edilebilir hâli: entity graph ile çözülmüş
     * satırlar + sunucuda hesaplanmış toplamlar (şablonda aritmetik yapılmaz).
     */
    public record OrderRequestDetail(OrderRequest request, List<Line> lines, BigDecimal total) {

        public record Line(OrderRequestItem item, BigDecimal lineTotal) {
        }
    }

    private final OrderRequestRepository orderRequestRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    public AdminInboxService(OrderRequestRepository orderRequestRepository,
                             ContactMessageRepository contactMessageRepository,
                             NewsletterSubscriberRepository newsletterSubscriberRepository) {
        this.orderRequestRepository = orderRequestRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
    }

    /* ── Sipariş talepleri ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Page<OrderRequest> orderRequests(InquiryStatus statusFilter, Pageable pageable) {
        return statusFilter != null
                ? orderRequestRepository.findByStatusOrderByCreatedAtDescIdDesc(statusFilter, pageable)
                : orderRequestRepository.findAllByOrderByCreatedAtDescIdDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<OrderRequestDetail> orderRequestWithItems(Long id) {
        return orderRequestRepository.findWithItemsById(id).map(AdminInboxService::toDetail);
    }

    @Transactional
    public void updateOrderStatus(Long id, InquiryStatus status) {
        OrderRequest request = orderRequestRepository.findById(id)
                .orElseThrow(AdminInboxService::notFound);
        request.setStatus(status);
        orderRequestRepository.save(request);
    }

    /* ── İletişim mesajları ──────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Page<ContactMessage> contactMessages(InquiryStatus statusFilter, Pageable pageable) {
        return statusFilter != null
                ? contactMessageRepository.findByStatusOrderByCreatedAtDescIdDesc(statusFilter, pageable)
                : contactMessageRepository.findAllByOrderByCreatedAtDescIdDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ContactMessage> contactMessage(Long id) {
        return contactMessageRepository.findById(id);
    }

    @Transactional
    public void updateMessageStatus(Long id, InquiryStatus status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(AdminInboxService::notFound);
        message.setStatus(status);
        contactMessageRepository.save(message);
    }

    /* ── Bülten aboneleri ────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Page<NewsletterSubscriber> subscribersPage(Pageable pageable) {
        return newsletterSubscriberRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Tüm abonelerin (çıkmışlar dahil — KVKK rıza kaydı) CSV dökümü.
     * UTF-8 BOM önekiyle başlar: Excel-TR aksi halde dosyayı Windows-1254
     * sanıp Türkçe karakterleri bozar. Zaman damgaları ISO-8601 (Instant).
     */
    @Transactional(readOnly = true)
    public String subscribersCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF').append("email,locale,kvkk_consent_at,unsubscribed_at\n");
        for (NewsletterSubscriber subscriber : newsletterSubscriberRepository.findAllByOrderByCreatedAtDesc()) {
            csv.append(csvField(subscriber.getEmail())).append(',')
                    .append(csvField(subscriber.getLocale())).append(',')
                    .append(csvField(subscriber.getKvkkConsentAt() != null
                            ? subscriber.getKvkkConsentAt().toString() : "")).append(',')
                    .append(csvField(subscriber.getUnsubscribedAt() != null
                            ? subscriber.getUnsubscribedAt().toString() : ""))
                    .append('\n');
        }
        return csv.toString();
    }

    /* ── Yardımcılar ─────────────────────────────────────────────────── */

    private static OrderRequestDetail toDetail(OrderRequest request) {
        List<OrderRequestDetail.Line> lines = request.getItems().stream()
                .map(item -> new OrderRequestDetail.Line(
                        item, item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();
        BigDecimal total = lines.stream()
                .map(OrderRequestDetail.Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderRequestDetail(request, lines, total);
    }

    /**
     * RFC 4180 tırnaklama + CSV formül enjeksiyonu savunması: =,+,-,@ ile
     * başlayan değer (örn. e-posta) Excel/Sheets'te formül olarak çalışmasın
     * diye tırnaklanır ve önüne ' eklenir.
     */
    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        boolean formulaRisk = !value.isEmpty() && "=+-@\t".indexOf(value.charAt(0)) >= 0;
        if (formulaRisk) {
            value = "'" + value;
        }
        if (formulaRisk || value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
