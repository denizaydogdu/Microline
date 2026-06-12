package tr.com.microline.admin.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tr.com.microline.admin.dto.StatusForm;
import tr.com.microline.admin.service.AdminInboxService;
import tr.com.microline.admin.service.AdminInboxService.OrderRequestDetail;
import tr.com.microline.entity.ContactMessage;
import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.entity.NewsletterSubscriber;
import tr.com.microline.entity.OrderRequest;
import tr.com.microline.i18n.PathLocaleResolver;
import tr.com.microline.i18n.Prices;

/**
 * Gelen kutusu: sipariş talepleri, iletişim mesajları, bülten aboneleri.
 * Listeler 20/sayfa sayfalanır; ?durum= filtresi geçersiz değerde sessizce
 * yok sayılır (filtre linkleri tek kaynak). Durum POST'ları PRG + flash;
 * geçersiz durum string'i binding hatasıdır — kayıt DEĞİŞMEZ, detay sayfası
 * uyarı flash'ıyla yeniden açılır. Tarih/fiyat biçimleme şablonda değil
 * burada yapılır (Instant zone ister; admin tek dil TR).
 */
@Controller
public class AdminInboxController {

    private static final int PAGE_SIZE = 20;

    /** Instant şablonda doğrudan biçimlenemez (zone gerekir) — burada çözülür. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Europe/Istanbul"));

    public record RequestRow(Long id, String createdAt, String fullName, String email, String statusKey) {
    }

    public record MessageRow(Long id, String createdAt, String name, String email,
                             String subject, String statusKey) {
    }

    public record SubscriberRow(String email, String locale, String kvkkConsentAt, boolean active) {
    }

    public record ItemRow(Long productId, String productName, String variantName,
                          int quantity, String unitPrice, String lineTotal) {
    }

    /** Mevcut durum dışındaki iki duruma geçiş butonu (hidden status + etiket anahtarı). */
    public record StatusAction(String value, String labelKey) {
    }

    private final AdminInboxService inboxService;
    private final Prices prices;

    public AdminInboxController(AdminInboxService inboxService, Prices prices) {
        this.inboxService = inboxService;
        this.prices = prices;
    }

    /* ── Sipariş talepleri ───────────────────────────────────────────── */

    @GetMapping("/admin/siparis-talepleri")
    public String orderRequests(@RequestParam(name = "durum", required = false) String durum,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                Model model) {
        InquiryStatus filter = parseStatus(durum);
        Page<OrderRequest> result = inboxService.orderRequests(filter, pageRequest(page));
        model.addAttribute("adminNav", "orders");
        model.addAttribute("rows", result.getContent().stream()
                .map(r -> new RequestRow(r.getId(), DATE_FORMAT.format(r.getCreatedAt()),
                        r.getFullName(), r.getEmail(), statusKey(r.getStatus())))
                .toList());
        addListModel(model, result, filter, "/admin/siparis-talepleri");
        return "admin/inbox/requests-list";
    }

    @GetMapping("/admin/siparis-talepleri/{id}")
    public String orderRequestDetail(@PathVariable Long id, Model model) {
        OrderRequestDetail detail = inboxService.orderRequestWithItems(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        OrderRequest request = detail.request();
        model.addAttribute("adminNav", "orders");
        model.addAttribute("req", request);
        model.addAttribute("createdAtText", DATE_FORMAT.format(request.getCreatedAt()));
        model.addAttribute("statusKey", statusKey(request.getStatus()));
        model.addAttribute("statusActions", statusActions(request.getStatus()));
        model.addAttribute("items", detail.lines().stream()
                .map(line -> new ItemRow(
                        line.item().getProduct().getId(),
                        line.item().getProduct().getNameTr(),
                        line.item().getVariant() != null ? line.item().getVariant().getNameTr() : null,
                        line.item().getQuantity(),
                        formatPrice(line.item().getUnitPrice()),
                        formatPrice(line.lineTotal())))
                .toList());
        model.addAttribute("totalText", formatPrice(detail.total()));
        return "admin/inbox/request-detail";
    }

    @PostMapping("/admin/siparis-talepleri/{id}/durum")
    public String updateOrderStatus(@PathVariable Long id,
                                    @Valid @ModelAttribute StatusForm statusForm,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("flashError", "admin.flash.invalid");
        } else {
            inboxService.updateOrderStatus(id, statusForm.status());
            redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        }
        return "redirect:/admin/siparis-talepleri/" + id;
    }

    /* ── İletişim mesajları ──────────────────────────────────────────── */

    @GetMapping("/admin/mesajlar")
    public String contactMessages(@RequestParam(name = "durum", required = false) String durum,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  Model model) {
        InquiryStatus filter = parseStatus(durum);
        Page<ContactMessage> result = inboxService.contactMessages(filter, pageRequest(page));
        model.addAttribute("adminNav", "messages");
        model.addAttribute("rows", result.getContent().stream()
                .map(m -> new MessageRow(m.getId(), DATE_FORMAT.format(m.getCreatedAt()),
                        m.getName(), m.getEmail(), m.getSubject(), statusKey(m.getStatus())))
                .toList());
        addListModel(model, result, filter, "/admin/mesajlar");
        return "admin/inbox/messages-list";
    }

    @GetMapping("/admin/mesajlar/{id}")
    public String contactMessageDetail(@PathVariable Long id, Model model) {
        ContactMessage message = inboxService.contactMessage(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("adminNav", "messages");
        model.addAttribute("msg", message);
        model.addAttribute("createdAtText", DATE_FORMAT.format(message.getCreatedAt()));
        model.addAttribute("statusKey", statusKey(message.getStatus()));
        model.addAttribute("statusActions", statusActions(message.getStatus()));
        return "admin/inbox/message-detail";
    }

    @PostMapping("/admin/mesajlar/{id}/durum")
    public String updateMessageStatus(@PathVariable Long id,
                                      @Valid @ModelAttribute StatusForm statusForm,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("flashError", "admin.flash.invalid");
        } else {
            inboxService.updateMessageStatus(id, statusForm.status());
            redirectAttributes.addFlashAttribute("flashSuccess", "admin.flash.statusChanged");
        }
        return "redirect:/admin/mesajlar/" + id;
    }

    /* ── Bülten aboneleri ────────────────────────────────────────────── */

    @GetMapping("/admin/aboneler")
    public String subscribers(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Page<NewsletterSubscriber> result = inboxService.subscribersPage(pageRequest(page));
        model.addAttribute("adminNav", "subscribers");
        model.addAttribute("rows", result.getContent().stream()
                .map(s -> new SubscriberRow(s.getEmail(), s.getLocale(),
                        DATE_FORMAT.format(s.getKvkkConsentAt()), s.getUnsubscribedAt() == null))
                .toList());
        model.addAttribute("subscriberTotal", result.getTotalElements());
        addListModel(model, result, null, "/admin/aboneler");
        return "admin/inbox/subscribers";
    }

    @GetMapping("/admin/aboneler/disa-aktar")
    public ResponseEntity<byte[]> exportSubscribersCsv() {
        byte[] body = inboxService.subscribersCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aboneler.csv\"")
                .body(body);
    }

    /* ── Yardımcılar ─────────────────────────────────────────────────── */

    /** Geçersiz/boş ?durum= değeri filtresiz listeye düşer (link'ler tek kaynak). */
    private static InquiryStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return InquiryStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static PageRequest pageRequest(int page) {
        return PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    }

    /** ROOT locale: TR locale'in I→ı dönüşümü CSS sınıfı/mesaj anahtarını bozar. */
    private static String statusKey(InquiryStatus status) {
        return status.name().toLowerCase(Locale.ROOT);
    }

    /** Mevcut durum hariç iki geçiş butonu; etiketler talep/mesaj sayfalarında ortaktır. */
    private static List<StatusAction> statusActions(InquiryStatus current) {
        return Arrays.stream(InquiryStatus.values())
                .filter(s -> s != current)
                .map(s -> new StatusAction(s.name(), markLabelKey(s)))
                .toList();
    }

    private static String markLabelKey(InquiryStatus status) {
        return switch (status) {
            case NEW -> "admin.requests.markNew";
            case CONTACTED -> "admin.requests.markContacted";
            case CLOSED -> "admin.requests.markClosed";
        };
    }

    /** Liste sayfalarının ortak model yükü: sayfalama + aktif filtre + temel yol. */
    private static void addListModel(Model model, Page<?> result, InquiryStatus filter, String basePath) {
        model.addAttribute("statusFilter", filter != null ? filter.name() : null);
        model.addAttribute("basePath", basePath);
        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("totalPages", Math.max(result.getTotalPages(), 1));
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
    }

    private String formatPrice(BigDecimal amount) {
        return prices.format(amount, PathLocaleResolver.TURKISH);
    }
}
