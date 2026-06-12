package tr.com.microline.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import tr.com.microline.entity.Difficulty;

/**
 * Eğitim create/update formu. Slug alanları boş bırakılabilir — servis
 * başlıktan üretir; doluysa pattern zorunlu. coverImage opsiyoneldir;
 * boş gelirse mevcut görsel korunur. Yayın tarihi formdan gelmez:
 * ilk yayına geçişte servis damgalar.
 */
public record TutorialForm(
        Long id,

        @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "{admin.validation.slug}")
        @Size(max = 200, message = "{validation.toolong}")
        String slugTr,

        @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "{admin.validation.slug}")
        @Size(max = 200, message = "{validation.toolong}")
        String slugEn,

        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.toolong}")
        String titleTr,

        @Size(max = 200, message = "{validation.toolong}")
        String titleEn,

        @NotBlank(message = "{validation.required}")
        @Size(max = 500, message = "{validation.toolong}")
        String excerptTr,

        @Size(max = 500, message = "{validation.toolong}")
        String excerptEn,

        @NotBlank(message = "{validation.required}")
        String bodyTr,

        String bodyEn,

        @Size(max = 300, message = "{validation.toolong}")
        String videoUrl,

        @NotNull(message = "{validation.required}")
        Difficulty difficulty,

        // Wrapper: işaretsiz checkbox null gelir (= taslak)
        Boolean published,

        MultipartFile coverImage) {
}
