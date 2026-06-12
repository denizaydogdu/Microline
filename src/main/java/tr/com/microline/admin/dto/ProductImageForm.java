package tr.com.microline.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/** Yeni ürün görseli yükleme formu; dosya içerik kontrolü ImageStorageService'te. */
public record ProductImageForm(
        MultipartFile file,

        @NotBlank(message = "{validation.required}")
        @Size(max = 300, message = "{validation.toolong}")
        String altTr,

        @Size(max = 300, message = "{validation.toolong}")
        String altEn) {
}
