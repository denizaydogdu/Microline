package tr.com.microline.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tr.com.microline.TestFlywayConfig;
import tr.com.microline.entity.AdminUser;
import tr.com.microline.repository.AdminUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class AdminSecurityTests {

    private static final String ADMIN_PASSWORD = "test-admin-pass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * AdminUserInitializer bu context açılırken admin'i yaratır; ama BAŞKA bir
     * test context'i (farklı konfigürasyonlu sınıflar) sonradan açılırsa
     * TestFlywayConfig DB'yi yeniden clean+migrate eder ve satır silinir —
     * initializer bu context'te tekrar koşmaz. İdempotent tohum bunu kapatır.
     */
    @BeforeEach
    void ensureAdminExists() {
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setDisplayName("Yönetici");
            admin.setEnabled(true);
            adminUserRepository.save(admin);
        }
    }

    private MockHttpSession loginSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin("/admin/giris")
                        .user("admin").password(ADMIN_PASSWORD))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/admin"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void anonymousAdminRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/giris"));
    }

    @Test
    void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/admin/giris"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void validCredentialsAuthenticateAndRedirectToDashboard() throws Exception {
        loginSession();
    }

    @Test
    void wrongPasswordRedirectsBackWithError() throws Exception {
        mockMvc.perform(formLogin("/admin/giris").user("admin").password("yanlis-sifre"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/admin/giris?error"));
    }

    @Test
    void dashboardRendersForAuthenticatedAdmin() throws Exception {
        MockHttpSession session = loginSession();
        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("Genel Bakış")));
    }

    @Test
    void authenticatedUserOnLoginPageIsRedirectedToDashboard() throws Exception {
        MockHttpSession session = loginSession();
        mockMvc.perform(get("/admin/giris").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void logoutInvalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = loginSession();
        mockMvc.perform(post("/admin/cikis").session(session).with(csrf()))
                .andExpect(redirectedUrl("/admin/giris?logout"));

        // Aynı session ile /admin artık girişe yönlenmeli
        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/giris"));
    }

    /* --- Public zincir regresyonu: admin zinciri eklenince site bozulmamalı --- */

    @Test
    void publicHomeStillRendersWithGlobalModelAdvice() throws Exception {
        // GlobalModelAdvice daraltıldı (basePackageClasses) — public model bozulmamalı
        mockMvc.perform(get("/tr/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("currentLang", "routes", "navCollections", "cartCount"))
                .andExpect(content().string(containsString("shop-menu")));
    }

    @Test
    void publicCartPostWithoutCsrfIsStillForbidden() throws Exception {
        mockMvc.perform(post("/tr/sepet/ekle")
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().isForbidden());
    }
}
