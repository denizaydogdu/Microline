package tr.com.microline.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Şifre eşleşme/doğruluk kontrolleri controller+serviste yapılır (mevcut
 * şifre PasswordEncoder.matches ister, anotasyonla ifade edilemez).
 */
public record PasswordChangeForm(
        @NotBlank(message = "{validation.required}")
        String currentPassword,

        @NotBlank(message = "{validation.required}")
        @Size(min = 10, message = "{admin.password.tooShort}")
        String newPassword,

        @NotBlank(message = "{validation.required}")
        String newPasswordConfirm) {
}
