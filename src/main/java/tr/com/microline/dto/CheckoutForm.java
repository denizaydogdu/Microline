package tr.com.microline.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Checkout yalnızca iletişim alanlarını taşır: sipariş satırları formdan
 * değil sunucudaki session sepetinden gelir (tamper yüzeyi yok).
 * website = honeypot, formRenderedAt = min doldurma süresi kontrolü.
 */
public record CheckoutForm(
        @NotBlank(message = "{validation.required}")
        @Size(max = 150, message = "{validation.toolong}")
        String fullName,

        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        @Size(max = 254, message = "{validation.toolong}")
        String email,

        @NotBlank(message = "{validation.required}")
        @Pattern(regexp = "[0-9+()\\s-]{7,20}", message = "{validation.phone}")
        String phone,

        @Size(max = 2000, message = "{validation.toolong}")
        String message,

        @NotNull(message = "{validation.kvkk}")
        @AssertTrue(message = "{validation.kvkk}")
        Boolean kvkkConsent,

        String website,
        Long formRenderedAt) {
}
