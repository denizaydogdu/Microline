package tr.com.microline.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Mevcut görselin alt metin + sıra düzenlemesi (görsel yöneticisi satır mini-formu). */
public record ImageMetaForm(
        @NotBlank(message = "{validation.required}")
        @Size(max = 300, message = "{validation.toolong}")
        String altTr,

        @Size(max = 300, message = "{validation.toolong}")
        String altEn,

        @NotNull(message = "{validation.required}")
        @Min(value = 0, message = "{admin.validation.sortOrder}")
        Integer sortOrder) {
}
