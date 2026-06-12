package tr.com.microline.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.dto.CartView;
import tr.com.microline.entity.Product;
import tr.com.microline.entity.ProductVariant;
import tr.com.microline.repository.ProductRepository;
import tr.com.microline.repository.ProductVariantRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class CartFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private Product flagship() {
        return productRepository.findBySlugTrAndActiveTrue("microline-kesim-makinesi").orElseThrow();
    }

    private Product addOn() {
        return productRepository.findBySlugTrAndActiveTrue("prokesim-bicak-modulu").orElseThrow();
    }

    private CartView cartView(MockHttpSession session) throws Exception {
        MvcResult result = mockMvc.perform(get("/tr/sepet").session(session))
                .andExpect(status().isOk())
                .andReturn();
        return (CartView) result.getModelAndView().getModel().get("cart");
    }

    private void add(MockHttpSession session, long productId, Long variantId, int qty) throws Exception {
        var request = post("/tr/sepet/ekle").session(session).with(csrf())
                .param("productId", String.valueOf(productId))
                .param("quantity", String.valueOf(qty));
        if (variantId != null) {
            request = request.param("variantId", String.valueOf(variantId));
        }
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
    }

    @Test
    void addRedirectsBackToProductPageWithBannerAnchor() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/tr/sepet/ekle").session(session).with(csrf())
                        .param("productId", flagship().getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tr/urun/*#cart-banner"));
    }

    @Test
    void sameProductAndVariantMergesIntoSingleLine() throws Exception {
        MockHttpSession session = new MockHttpSession();
        long productId = flagship().getId();
        add(session, productId, null, 2);
        add(session, productId, null, 3);

        CartView cart = cartView(session);
        assertThat(cart.lines()).hasSize(1);
        assertThat(cart.lines().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void variantLinesAreSeparateAndPricedWithDelta() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Product product = flagship();
        ProductVariant variant = productVariantRepository
                .findByProductAndActiveTrueOrderBySortOrder(product).stream()
                .filter(v -> v.getPriceDelta().signum() > 0)
                .findFirst().orElseThrow();

        add(session, product.getId(), null, 1);
        add(session, product.getId(), variant.getId(), 1);

        CartView cart = cartView(session);
        assertThat(cart.lines()).hasSize(2);
        assertThat(cart.lines())
                .anySatisfy(line -> assertThat(line.unitPrice())
                        .isEqualByComparingTo(product.getPriceAmount().add(variant.getPriceDelta())));
        assertThat(cart.total())
                .isEqualByComparingTo(product.getPriceAmount()
                        .add(product.getPriceAmount().add(variant.getPriceDelta())));
    }

    @Test
    void updateAndRemoveLine() throws Exception {
        MockHttpSession session = new MockHttpSession();
        long flagshipId = flagship().getId();
        long addOnId = addOn().getId();
        add(session, flagshipId, null, 1);
        add(session, addOnId, null, 1);

        mockMvc.perform(post("/tr/sepet/guncelle").session(session).with(csrf())
                        .param("productId", String.valueOf(flagshipId))
                        .param("quantity", "7"))
                .andExpect(status().is3xxRedirection());

        CartView afterUpdate = cartView(session);
        assertThat(afterUpdate.lines())
                .anySatisfy(line -> {
                    if (line.product().getId().equals(flagshipId)) {
                        assertThat(line.quantity()).isEqualTo(7);
                    }
                });

        mockMvc.perform(post("/tr/sepet/kaldir").session(session).with(csrf())
                        .param("productId", String.valueOf(addOnId)))
                .andExpect(status().is3xxRedirection());

        CartView afterRemove = cartView(session);
        assertThat(afterRemove.lines()).hasSize(1);
        assertThat(afterRemove.lines().get(0).product().getId()).isEqualTo(flagshipId);
    }

    @Test
    void tamperedProductIdLeavesCartUnchanged() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/tr/sepet/ekle").session(session).with(csrf())
                        .param("productId", "999999")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        CartView cart = cartView(session);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void foreignVariantIsRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Product other = addOn();
        ProductVariant flagshipVariant = productVariantRepository
                .findByProductAndActiveTrueOrderBySortOrder(flagship()).get(0);

        // Başka ürünün varyantıyla ekleme: satır oluşmamalı
        add(session, other.getId(), flagshipVariant.getId(), 1);

        CartView cart = cartView(session);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void quantityIsClampedServerSide() throws Exception {
        MockHttpSession session = new MockHttpSession();
        add(session, flagship().getId(), null, 5000);

        CartView cart = cartView(session);
        assertThat(cart.lines().get(0).quantity()).isEqualTo(999);
    }

    @Test
    void englishCartPageRendersWithRoute() throws Exception {
        MockHttpSession session = new MockHttpSession();
        add(session, flagship().getId(), null, 1);

        mockMvc.perform(get("/en/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("cart", "route"));
    }
}
