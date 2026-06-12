package tr.com.microline.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewsletterForm(
        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        @Size(max = 254, message = "{validation.toolong}")
        String email,

        @NotNull(message = "{validation.kvkk}")
        @AssertTrue(message = "{validation.kvkk}")
        Boolean kvkkConsent,

        String website) {
}
