package tr.com.microline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

/**
 * İki ayrı zincir: @Order(1) /admin/** (formLogin + ROLE_ADMIN), @Order(2)
 * geri kalan public site. loginProcessingUrl da /admin/** altında olmak
 * zorunda — aksi hâlde POST public zincire düşer ve giriş döngüye girer.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/giris").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/giris")
                        .loginProcessingUrl("/admin/giris")
                        .defaultSuccessUrl("/admin", false)
                        .failureUrl("/admin/giris?error"))
                .logout(logout -> logout
                        .logoutUrl("/admin/cikis")
                        .logoutSuccessUrl("/admin/giris?logout")
                        .invalidateHttpSession(true))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId()));
        applyEagerCsrfTokenResolution(http);
        applySharedHeaders(http);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        // CSRF varsayılan olarak açık kalır (Thymeleaf th:action token'ı basar).
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        applyEagerCsrfTokenResolution(http);
        applySharedHeaders(http);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Deferred CSRF token'ı render başlamadan çözer: Thymeleaf çıktıyı
     * akıtırken (commit sonrası) session yaratılamaz; token sayfanın
     * altındaki bir formda ilk kez çözülürse 500 yerine yarım sayfa
     * oluşur. Erken çözüm session'ı güvenli anda kurar.
     * HER İKİ zincire de uygulanmak ZORUNDA — admin zincirinde unutulursa
     * yarım-sayfa bug'ı oradan geri döner.
     */
    private void applyEagerCsrfTokenResolution(HttpSecurity http) throws Exception {
        http.addFilterAfter((request, response, chain) -> {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            chain.doFilter(request, response);
        }, CsrfFilter.class);
    }

    /** CSP/XFO/referrer politikaları iki zincirde de birebir aynıdır. */
    private void applySharedHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'")));
    }
}
