package tr.com.microline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * v1'de login yok: her şey public, /admin/** v2'ye kadar denyAll.
     * CSRF varsayılan olarak açık kalır (Thymeleaf th:action token'ı basar).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").denyAll()
                        .anyRequest().permitAll())
                /*
                 * Deferred CSRF token'ı render başlamadan çözer: Thymeleaf çıktıyı
                 * akıtırken (commit sonrası) session yaratılamaz; token sayfanın
                 * altındaki bir formda ilk kez çözülürse 500 yerine yarım sayfa
                 * oluşur. Erken çözüm session'ı güvenli anda kurar.
                 */
                .addFilterAfter((request, response, chain) -> {
                    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrfToken != null) {
                        csrfToken.getToken();
                    }
                    chain.doFilter(request, response);
                }, CsrfFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'")));
        return http.build();
    }
}
