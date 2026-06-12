package tr.com.microline.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactForm(
        @NotBlank(message = "{validation.required}")
        @Size(max = 150, message = "{validation.toolong}")
        String name,

        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        @Size(max = 254, message = "{validation.toolong}")
        String email,

        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.toolong}")
        String subject,

        @NotBlank(message = "{validation.required}")
        @Size(max = 5000, message = "{validation.toolong}")
        String message,

        @NotNull(message = "{validation.kvkk}")
        @AssertTrue(message = "{validation.kvkk}")
        Boolean kvkkConsent,

        String website,
        Long formRenderedAt) {
}
