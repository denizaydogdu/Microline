package tr.com.microline.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yasal sayfa düzenleme formu: yalnız başlık + gövde. code ve slug'lar
 * bilinçli olarak YOK — 9 sabit sayfanın URL'leri site içi linklerde ve
 * PageRoute dışı fallback'te kullanılır, panelden değiştirilemez.
 */
public record LegalPageForm(
        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.toolong}")
        String titleTr,

        @Size(max = 200, message = "{validation.toolong}")
        String titleEn,

        @NotBlank(message = "{validation.required}")
        String bodyTr,

        String bodyEn) {
}
