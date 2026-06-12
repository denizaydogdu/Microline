package tr.com.microline.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * Koleksiyon düzenleme formu (create yok — 4 sabit enum kod). heroImage
 * opsiyoneldir; boş gelirse mevcut görsel korunur.
 */
public record CollectionForm(
        Long id,

        @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "{admin.validation.slug}")
        @Size(max = 200, message = "{validation.toolong}")
        String slugTr,

        @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "{admin.validation.slug}")
        @Size(max = 200, message = "{validation.toolong}")
        String slugEn,

        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.toolong}")
        String nameTr,

        @Size(max = 200, message = "{validation.toolong}")
        String nameEn,

        @NotBlank(message = "{validation.required}")
        String descriptionTr,

        String descriptionEn,

        @NotNull(message = "{validation.required}")
        @Min(value = 0, message = "{admin.validation.sortOrder}")
        Integer sortOrder,

        // Wrapper: işaretsiz checkbox null gelir (= pasif)
        Boolean active,

        MultipartFile heroImage) {
}
