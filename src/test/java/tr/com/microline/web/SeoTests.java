package tr.com.microline.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tr.com.microline.TestFlywayConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class SeoTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sitemapListsBothLocalesWithAlternates() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/tr/urun/microline-kesim-makinesi")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/en/products/microline-cutting-machine")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hreflang=\"x-default\"")))
                // İşlemsel sayfalar sitemap'e girmez
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/tr/sepet"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/tr/siparis-talebi"))));
    }

    @Test
    void robotsDisallowsTransactionalPagesAndPointsToSitemap() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Disallow: /tr/sepet")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sitemap: ")));
    }

    @Test
    void homePageCarriesCanonicalAndHreflang() throws Exception {
        mockMvc.perform(get("/tr/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("rel=\"canonical\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hreflang=\"en\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("application/ld+json")));
    }

    @Test
    void productPageCarriesProductJsonLd() throws Exception {
        mockMvc.perform(get("/tr/urun/microline-kesim-makinesi"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"@type\": \"Product\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("og:title")));
    }
}
