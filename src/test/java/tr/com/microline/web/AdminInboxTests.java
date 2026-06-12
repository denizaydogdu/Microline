package tr.com.microline.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.entity.ContactMessage;
import tr.com.microline.entity.InquiryStatus;
import tr.com.microline.entity.NewsletterSubscriber;
import tr.com.microline.entity.OrderRequest;
import tr.com.microline.entity.OrderRequestItem;
import tr.com.microline.entity.Product;
import tr.com.microline.repository.ContactMessageRepository;
import tr.com.microline.repository.NewsletterSubscriberRepository;
import tr.com.microline.repository.OrderRequestRepository;
import tr.com.microline.repository.ProductRepository;

/**
 * Gelen kutusu (talep/mesaj/abone) uçtan uca. Tohum veriler deterministlik
 * için repository'den doğrudan yazılır (sepet akışı CartFlowTests'te zaten
 * kapsanıyor). Test DB'si context'ler arasında paylaşıldığından her test
 * kendi kayıtlarını oluşturur ve finally bloğunda siler.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class AdminInboxTests {

    private static final String ORDER_NAME = "İnbox Test Müşterisi";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Autowired
    private ProductRepository productRepository;

    /** UserDetailsService'e dokunmadan ROLE_ADMIN kimliği basar. */
    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    /** ML-C1 ürününden 2 adetlik tek kalemli talep (NEW) — cascade items'ı da yazar. */
    private OrderRequest seedOrder() {
        Product product = productRepository.findAll().stream()
                .filter(p -> "ML-C1".equals(p.getSku()))
                .findFirst().orElseThrow();
        OrderRequest request = new OrderRequest();
        request.setFullName(ORDER_NAME);
        request.setEmail("inbox-test@example.com");
        request.setPhone("+90 555 111 22 33");
        request.setMessage("Sınıfım için iki adet rica ediyorum.");
        request.setKvkkConsent(true);
        request.setLocale("tr");
        request.setSourceIp("203.0.113.7");
        OrderRequestItem item = new OrderRequestItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("4990.00"));
        item.setCurrency("TRY");
        request.addItem(item);
        return orderRequestRepository.save(request);
    }

    private ContactMessage seedMessage(String subject) {
        ContactMessage message = new ContactMessage();
        message.setName("İnbox Mesaj Testi");
        message.setEmail("inbox-mesaj@example.com");
        message.setSubject(subject);
        message.setMessage("Toplu alım için fiyat bilgisi rica ederim.");
        message.setKvkkConsent(true);
        message.setLocale("tr");
        return contactMessageRepository.save(message);
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int from = 0;
        while ((from = haystack.indexOf(needle, from)) != -1) {
            count++;
            from += needle.length();
        }
        return count;
    }

    /* ── Sipariş talepleri ───────────────────────────────────────────── */

    @Test
    void requestListRendersAndStatusFilterWorks() throws Exception {
        OrderRequest order = seedOrder();
        try {
            mockMvc.perform(get("/admin/siparis-talepleri").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(ORDER_NAME)));
            mockMvc.perform(get("/admin/siparis-talepleri").param("durum", "NEW").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(ORDER_NAME)));
            mockMvc.perform(get("/admin/siparis-talepleri").param("durum", "CLOSED").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString(ORDER_NAME))));
            // Geçersiz filtre değeri sessizce yok sayılır → filtresiz liste
            mockMvc.perform(get("/admin/siparis-talepleri").param("durum", "BOZUK").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(ORDER_NAME)));
        } finally {
            orderRequestRepository.delete(order);
        }
    }

    /** Kalem + ürün adı + fiyatlar render olur — @EntityGraph kanıtı (graph'sız LazyInitException). */
    @Test
    void requestDetailRendersItemsViaEntityGraph() throws Exception {
        OrderRequest order = seedOrder();
        String productName = productRepository.findAll().stream()
                .filter(p -> "ML-C1".equals(p.getSku()))
                .findFirst().orElseThrow().getNameTr();
        try {
            mockMvc.perform(get("/admin/siparis-talepleri/" + order.getId()).with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(productName)))
                    .andExpect(content().string(containsString("4.990 ₺")))   // birim fiyat (TR biçim)
                    .andExpect(content().string(containsString("9.980 ₺")))   // 2 × 4.990 genel toplam
                    .andExpect(content().string(containsString("203.0.113.7")));
        } finally {
            orderRequestRepository.delete(order);
        }
    }

    @Test
    void requestStatusUpdatePersistsAndInvalidValueIsIgnored() throws Exception {
        OrderRequest order = seedOrder();
        try {
            mockMvc.perform(post("/admin/siparis-talepleri/" + order.getId() + "/durum")
                            .param("status", "CONTACTED").with(admin()).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/siparis-talepleri/" + order.getId()));
            assertThat(orderRequestRepository.findById(order.getId()).orElseThrow().getStatus())
                    .isEqualTo(InquiryStatus.CONTACTED);

            // Geçersiz durum string'i binding hatasıdır: PRG + uyarı flash'ı, kayıt DEĞİŞMEZ
            mockMvc.perform(post("/admin/siparis-talepleri/" + order.getId() + "/durum")
                            .param("status", "BOZUK").with(admin()).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/siparis-talepleri/" + order.getId()));
            assertThat(orderRequestRepository.findById(order.getId()).orElseThrow().getStatus())
                    .isEqualTo(InquiryStatus.CONTACTED);
        } finally {
            orderRequestRepository.delete(order);
        }
    }

    /* ── İletişim mesajları ──────────────────────────────────────────── */

    @Test
    void messageListDetailAndStatusUpdateWork() throws Exception {
        ContactMessage message = seedMessage("İnbox Test Konusu");
        try {
            mockMvc.perform(get("/admin/mesajlar").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("İnbox Test Konusu")));
            mockMvc.perform(get("/admin/mesajlar").param("durum", "CLOSED").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("İnbox Test Konusu"))));
            mockMvc.perform(get("/admin/mesajlar/" + message.getId()).with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Toplu alım için fiyat bilgisi rica ederim.")))
                    .andExpect(content().string(containsString("inbox-mesaj@example.com")));
            mockMvc.perform(post("/admin/mesajlar/" + message.getId() + "/durum")
                            .param("status", "CONTACTED").with(admin()).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/mesajlar/" + message.getId()));
            assertThat(contactMessageRepository.findById(message.getId()).orElseThrow().getStatus())
                    .isEqualTo(InquiryStatus.CONTACTED);
        } finally {
            contactMessageRepository.delete(message);
        }
    }

    /** 25 mesaj → sayfa 0'da 20 satır, sayfa 1'de 5 satır (20/sayfa). */
    @Test
    void messageListPaginatesTwentyPerPage() throws Exception {
        List<ContactMessage> created = new ArrayList<>();
        try {
            for (int i = 0; i < 25; i++) {
                created.add(seedMessage("SayfalamaTesti-" + i + "-Konu"));
                // created_at eşitliği sayfa sınırında satır kaydırabilir — milisaniye garantisi
                Thread.sleep(2);
            }
            MvcResult page0 = mockMvc.perform(get("/admin/mesajlar").with(admin()))
                    .andExpect(status().isOk()).andReturn();
            MvcResult page1 = mockMvc.perform(get("/admin/mesajlar").param("page", "1").with(admin()))
                    .andExpect(status().isOk()).andReturn();
            assertThat(countOccurrences(page0.getResponse().getContentAsString(StandardCharsets.UTF_8),
                    "SayfalamaTesti-")).isEqualTo(20);
            assertThat(countOccurrences(page1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                    "SayfalamaTesti-")).isEqualTo(5);
        } finally {
            contactMessageRepository.deleteAll(created);
        }
    }

    /* ── Aboneler + CSV ──────────────────────────────────────────────── */

    @Test
    void subscribersPageRendersAndCsvExportHasBomHeaderAndRows() throws Exception {
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail("inbox-csv-test@example.com");
        subscriber.setLocale("tr");
        subscriber.setKvkkConsentAt(Instant.parse("2026-06-01T10:00:00Z"));
        subscriber.setUnsubscribeToken(UUID.randomUUID());
        subscriber = newsletterSubscriberRepository.save(subscriber);
        try {
            mockMvc.perform(get("/admin/aboneler").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("inbox-csv-test@example.com")));

            MvcResult csv = mockMvc.perform(get("/admin/aboneler/disa-aktar").with(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/csv"))
                    .andReturn();
            String body = csv.getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(body).startsWith("\uFEFF");                                // Excel-TR BOM'u
            assertThat(body).contains("email,locale,kvkk_consent_at,unsubscribed_at");
            assertThat(body).contains("inbox-csv-test@example.com,tr,2026-06-01T10:00:00Z,");
            assertThat(csv.getResponse().getHeader("Content-Disposition"))
                    .contains("attachment").contains("aboneler.csv");
        } finally {
            newsletterSubscriberRepository.delete(subscriber);
        }
    }

    /* ── Güvenlik ────────────────────────────────────────────────────── */

    @Test
    void anonymousInboxRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/siparis-talepleri"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/giris"));
    }
}
