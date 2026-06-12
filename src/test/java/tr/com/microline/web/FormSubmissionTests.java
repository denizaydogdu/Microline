package tr.com.microline.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.entity.Product;
import tr.com.microline.repository.ContactMessageRepository;
import tr.com.microline.repository.NewsletterSubscriberRepository;
import tr.com.microline.repository.OrderRequestItemRepository;
import tr.com.microline.repository.OrderRequestRepository;
import tr.com.microline.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class FormSubmissionTests {

    /** InquiryService min gönderim süresinin (3sn) güvenle üstünde bir render anı. */
    private static final long RENDERED_LONG_AGO = Instant.now().minusSeconds(60).toEpochMilli();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private OrderRequestItemRepository orderRequestItemRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product flagship() {
        return productRepository.findBySlugTrAndActiveTrue("microline-kesim-makinesi").orElseThrow();
    }

    /** Sepet session'da yaşar: ekleme ve checkout aynı MockHttpSession'ı paylaşmalı. */
    private MockHttpSession sessionWithCartLine(long productId, int quantity) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/tr/sepet/ekle").session(session).with(csrf())
                        .param("productId", String.valueOf(productId))
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().is3xxRedirection());
        return session;
    }

    @Test
    void checkoutWithEmptyCartRedirectsToCartPage() throws Exception {
        mockMvc.perform(get("/tr/siparis-talebi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/sepet"));
    }

    @Test
    void checkoutRendersWithCartInSession() throws Exception {
        MockHttpSession session = sessionWithCartLine(flagship().getId(), 1);

        mockMvc.perform(get("/tr/siparis-talebi").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("checkoutForm", "cart"));
    }

    @Test
    void legacyProductDeepLinkRedirectsToProductPage() throws Exception {
        mockMvc.perform(get("/tr/siparis-talebi").param("urun", "microline-kesim-makinesi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/urun/microline-kesim-makinesi"));
    }

    @Test
    void validCheckoutPersistsRequestWithItemsAndClearsCart() throws Exception {
        long requestsBefore = orderRequestRepository.count();
        long itemsBefore = orderRequestItemRepository.count();
        MockHttpSession session = sessionWithCartLine(flagship().getId(), 2);

        mockMvc.perform(post("/tr/siparis-talebi").session(session).with(csrf())
                        .param("fullName", "Ayşe Yılmaz")
                        .param("email", "ayse@example.com")
                        .param("phone", "+90 532 000 00 00")
                        .param("kvkkConsent", "true")
                        .param("formRenderedAt", String.valueOf(RENDERED_LONG_AGO)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/siparis-talebi"));

        assertThat(orderRequestRepository.count()).isEqualTo(requestsBefore + 1);
        assertThat(orderRequestItemRepository.count()).isEqualTo(itemsBefore + 1);
        assertThat(orderRequestItemRepository.findAll())
                .anySatisfy(item -> {
                    assertThat(item.getQuantity()).isEqualTo(2);
                    assertThat(item.getUnitPrice()).isEqualByComparingTo(flagship().getPriceAmount());
                });

        // PRG sonrası ilk GET flash formSuccess'i tüketir ve başarı sayfasını gösterir
        mockMvc.perform(get("/tr/siparis-talebi").session(session))
                .andExpect(status().isOk());

        // Sepet temizlendi: flash tükendikten sonraki GET sepete yönlendirir
        mockMvc.perform(get("/tr/siparis-talebi").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/sepet"));
    }

    @Test
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/tr/siparis-talebi")
                        .param("fullName", "Ayşe Yılmaz")
                        .param("email", "ayse@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cartAddWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/tr/sepet/ekle")
                        .param("productId", flagship().getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingKvkkConsentIsRejectedAndNothingPersisted() throws Exception {
        long before = orderRequestRepository.count();
        MockHttpSession session = sessionWithCartLine(flagship().getId(), 1);

        mockMvc.perform(post("/tr/siparis-talebi").session(session).with(csrf())
                        .param("fullName", "Ayşe Yılmaz")
                        .param("email", "ayse@example.com")
                        .param("phone", "+90 532 000 00 00")
                        .param("formRenderedAt", String.valueOf(RENDERED_LONG_AGO)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("checkoutForm", "kvkkConsent"));

        assertThat(orderRequestRepository.count()).isEqualTo(before);
    }

    @Test
    void filledHoneypotPretendsSuccessButPersistsNothing() throws Exception {
        long before = orderRequestRepository.count();
        MockHttpSession session = sessionWithCartLine(flagship().getId(), 1);

        mockMvc.perform(post("/tr/siparis-talebi").session(session).with(csrf())
                        .param("fullName", "Bot Bot")
                        .param("email", "bot@example.com")
                        .param("phone", "+90 532 111 11 11")
                        .param("kvkkConsent", "true")
                        .param("website", "http://spam.example")
                        .param("formRenderedAt", String.valueOf(RENDERED_LONG_AGO)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/siparis-talebi"));

        assertThat(orderRequestRepository.count()).isEqualTo(before);
    }

    @Test
    void contactFormValidSubmissionPersists() throws Exception {
        long before = contactMessageRepository.count();

        mockMvc.perform(post("/en/contact").with(csrf())
                        .param("name", "John Maker")
                        .param("email", "john@example.com")
                        .param("subject", "Classroom set")
                        .param("message", "Do you offer educator pricing?")
                        .param("kvkkConsent", "true")
                        .param("formRenderedAt", String.valueOf(RENDERED_LONG_AGO)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/en/contact"));

        assertThat(contactMessageRepository.count()).isEqualTo(before + 1);
        assertThat(contactMessageRepository.findAll())
                .anySatisfy(m -> assertThat(m.getLocale()).isEqualTo("en"));
    }

    @Test
    void newsletterDuplicateEmailIsIdempotent() throws Exception {
        String email = "tekrar@example.com";

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/tr/bulten-abonelik").with(csrf())
                            .param("email", email)
                            .param("kvkkConsent", "true")
                            .param("redirect", "/tr/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tr/"));
        }

        assertThat(newsletterSubscriberRepository.findByEmail(email)).isPresent();
        assertThat(newsletterSubscriberRepository.findAll())
                .filteredOn(s -> s.getEmail().equals(email))
                .hasSize(1);
    }

    @Test
    void newsletterRejectsOffsiteRedirectTarget() throws Exception {
        mockMvc.perform(post("/tr/bulten-abonelik").with(csrf())
                        .param("email", "guvenli@example.com")
                        .param("kvkkConsent", "true")
                        .param("redirect", "https://evil.example/phish"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tr/"));
    }

    @Test
    void newsletterRejectsObfuscatedRedirectTargets() throws Exception {
        // Tarayıcı normalizasyonuyla dışarı kaçabilecek varyantlar fallback'e düşmeli
        for (String evil : new String[]{
                "/tr/\\evil.example", "\\\\evil.example", "/tr/../en/x",
                "//evil.example", "/tr//evil.example"}) {
            mockMvc.perform(post("/tr/bulten-abonelik").with(csrf())
                            .param("email", "guvenli2@example.com")
                            .param("kvkkConsent", "true")
                            .param("redirect", evil))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tr/"));
        }
    }
}
