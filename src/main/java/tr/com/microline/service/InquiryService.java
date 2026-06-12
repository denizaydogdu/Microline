package tr.com.microline.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.dto.CheckoutForm;
import tr.com.microline.dto.ContactForm;
import tr.com.microline.dto.NewsletterForm;
import tr.com.microline.entity.ContactMessage;
import tr.com.microline.entity.NewsletterSubscriber;
import tr.com.microline.entity.OrderRequest;
import tr.com.microline.entity.OrderRequestItem;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.repository.ContactMessageRepository;
import tr.com.microline.repository.NewsletterSubscriberRepository;
import tr.com.microline.repository.OrderRequestRepository;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;

/**
 * Form gönderimlerini kaydeder. Spam savunması (honeypot, min gönderim
 * süresi, IP başına hız sınırı) kayıttan ÖNCE çalışır ve bot'a başarısızlık
 * sızdırmamak için SPAM sonucunda controller başarı gibi davranır.
 */
@Service
public class InquiryService {

    public enum Result { SAVED, SPAM, EMPTY_CART }

    private static final Logger log = LoggerFactory.getLogger(InquiryService.class);

    /** Formun render edilmesiyle gönderimi arasındaki insanî alt sınır. */
    private static final Duration MIN_FILL_TIME = Duration.ofSeconds(3);
    private static final Duration THROTTLE_WINDOW = Duration.ofMinutes(10);

    /** Pencere başına IP limiti — testler aynı IP'den geldiği için profille yükseltilebilir. */
    private final int throttleMax;

    /**
     * Bellek-içi throttle: tek instance v1 için yeterli; restart'ta sıfırlanır.
     * Çok instance'lı v2'de Redis benzeri paylaşımlı sayaca taşınmalı.
     */
    private final Map<String, Deque<Instant>> submissionsByIp = new ConcurrentHashMap<>();

    private final OrderRequestRepository orderRequestRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public InquiryService(OrderRequestRepository orderRequestRepository,
                          ContactMessageRepository contactMessageRepository,
                          NewsletterSubscriberRepository newsletterSubscriberRepository,
                          ProductRepository productRepository,
                          ProductVariantRepository productVariantRepository,
                          @Value("${microline.throttle.max-per-window:5}") int throttleMax) {
        this.throttleMax = throttleMax;
        this.orderRequestRepository = orderRequestRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Sepetten gelen satırlarla tek talep yazar. Satırlar formdan değil
     * session sepetinden gelir; yine de transaction içinde yeniden doğrulanır
     * (render ile submit arasında ürün pasifleşmiş olabilir). Geçerli satır
     * kalmazsa EMPTY_CART döner; controller sepete yönlendirir.
     */
    @Transactional
    public Result saveOrderRequest(CheckoutForm form, List<Cart.CartLine> lines,
                                   Locale locale, String sourceIp) {
        if (isSpam(form.website(), form.formRenderedAt(), sourceIp)) {
            return Result.SPAM;
        }
        if (lines.isEmpty()) {
            return Result.EMPTY_CART;
        }

        OrderRequest entity = new OrderRequest();
        entity.setFullName(form.fullName().strip());
        entity.setEmail(form.email().strip());
        entity.setPhone(form.phone().strip());
        entity.setMessage(blankToNull(form.message()));
        entity.setKvkkConsent(Boolean.TRUE.equals(form.kvkkConsent()));
        entity.setLocale(lang(locale));
        entity.setSourceIp(sourceIp);

        for (Cart.CartLine line : lines) {
            Product product = productRepository.findById(line.productId())
                    .filter(Product::isActive)
                    .orElse(null);
            if (product == null) {
                continue;
            }
            ProductVariant variant = null;
            if (line.variantId() != null) {
                variant = productVariantRepository.findById(line.variantId())
                        .filter(v -> v.isActive() && v.getProduct().getId().equals(product.getId()))
                        .orElse(null);
                if (variant == null) {
                    continue;
                }
            }
            OrderRequestItem item = new OrderRequestItem();
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(Math.max(1, Math.min(999, line.quantity())));
            // Fiyat submit ANINDA snapshot'lanır: müşteri aramasında referans budur
            item.setUnitPrice(CartService.unitPrice(product, variant));
            item.setCurrency(product.getCurrency());
            entity.addItem(item);
        }

        if (entity.getItems().isEmpty()) {
            return Result.EMPTY_CART;
        }
        orderRequestRepository.save(entity);
        return Result.SAVED;
    }

    @Transactional
    public Result saveContactMessage(ContactForm form, Locale locale, String sourceIp) {
        if (isSpam(form.website(), form.formRenderedAt(), sourceIp)) {
            return Result.SPAM;
        }
        ContactMessage entity = new ContactMessage();
        entity.setName(form.name().strip());
        entity.setEmail(form.email().strip());
        entity.setSubject(form.subject().strip());
        entity.setMessage(form.message().strip());
        entity.setKvkkConsent(Boolean.TRUE.equals(form.kvkkConsent()));
        entity.setLocale(lang(locale));
        contactMessageRepository.save(entity);
        return Result.SAVED;
    }

    /**
     * Idempotent: kayıtlı ve aktif e-posta tekrar gelirse başarı sayılır;
     * abonelikten çıkmış e-posta yeni rıza zaman damgasıyla yeniden aktifleşir.
     */
    @Transactional
    public Result subscribeNewsletter(NewsletterForm form, Locale locale, String sourceIp) {
        if (isSpam(form.website(), null, sourceIp)) {
            return Result.SPAM;
        }
        String email = form.email().strip().toLowerCase(Locale.ROOT);
        NewsletterSubscriber subscriber = newsletterSubscriberRepository.findByEmail(email).orElse(null);
        if (subscriber == null) {
            subscriber = new NewsletterSubscriber();
            subscriber.setEmail(email);
            subscriber.setUnsubscribeToken(UUID.randomUUID());
        } else if (subscriber.getUnsubscribedAt() == null) {
            return Result.SAVED;
        }
        subscriber.setLocale(lang(locale));
        subscriber.setKvkkConsentAt(Instant.now());
        subscriber.setUnsubscribedAt(null);
        newsletterSubscriberRepository.save(subscriber);
        return Result.SAVED;
    }

    /** formRenderedAt=null olan formlarda (bülten) yalnızca honeypot+throttle bakılır. */
    private boolean isSpam(String honeypot, Long formRenderedAt, String sourceIp) {
        if (honeypot != null && !honeypot.isBlank()) {
            log.info("Honeypot dolu geldi, gönderim atlandı (ip={})", sourceIp);
            return true;
        }
        if (formRenderedAt != null
                && Instant.ofEpochMilli(formRenderedAt).plus(MIN_FILL_TIME).isAfter(Instant.now())) {
            log.info("Form {}sn'den hızlı gönderildi, spam sayıldı (ip={})", MIN_FILL_TIME.toSeconds(), sourceIp);
            return true;
        }
        return isThrottled(sourceIp);
    }

    private boolean isThrottled(String sourceIp) {
        if (sourceIp == null || sourceIp.isBlank()) {
            return false;
        }
        Instant cutoff = Instant.now().minus(THROTTLE_WINDOW);
        Deque<Instant> times = submissionsByIp.computeIfAbsent(sourceIp, ip -> new ConcurrentLinkedDeque<>());
        // size()+add() atomik değil: kilitsiz hâlde eşzamanlı istekler limiti aşar
        synchronized (times) {
            times.removeIf(t -> t.isBefore(cutoff));
            if (times.size() >= throttleMax) {
                log.info("IP hız sınırına takıldı: {}", sourceIp);
                return true;
            }
            times.add(Instant.now());
        }
        pruneStaleIps(cutoff);
        return false;
    }

    /**
     * Boşalan deque'ler map'ten silinmezse benzersiz IP taraması (özellikle
     * IPv6) map'i sınırsız büyütür. Her gönderimde ucuz bir süpürme yeterli.
     */
    private void pruneStaleIps(Instant cutoff) {
        submissionsByIp.entrySet().removeIf(entry -> {
            Deque<Instant> deque = entry.getValue();
            synchronized (deque) {
                deque.removeIf(t -> t.isBefore(cutoff));
                return deque.isEmpty();
            }
        });
    }

    private String lang(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? "en" : "tr";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
