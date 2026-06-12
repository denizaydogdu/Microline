package tr.com.microline.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Ürün create/update formu. Slug alanları boş bırakılabilir — servis addan
 * üretir; doluysa pattern zorunlu. specsJson sözdizimi anotasyonla değil
 * serviste (ObjectMapper.readTree) doğrulanır. Kopyalama daima form→entity
 * yönündedir, entity'ye binding yapılmaz.
 */
public record ProductForm(
        Long id,

        @NotBlank(message = "{validation.required}")
        @Size(max = 40, message = "{validation.toolong}")
        String sku,

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
        String taglineTr,

        String taglineEn,

        @NotBlank(message = "{validation.required}")
        String descriptionTr,

        String descriptionEn,

        String safetyNotesTr,

        String safetyNotesEn,

        String specsJson,

        @NotNull(message = "{validation.required}")
        @DecimalMin(value = "0.01", message = "{admin.validation.price}")
        @Digits(integer = 8, fraction = 2, message = "{admin.validation.price}")
        BigDecimal priceAmount,

        @DecimalMin(value = "0.01", message = "{admin.validation.price}")
        @Digits(integer = 8, fraction = 2, message = "{admin.validation.price}")
        BigDecimal compareAtPrice,

        @NotNull(message = "{validation.required}")
        Long collectionId,

        // Checkbox işaretsizken parametre hiç gelmez: primitive boolean
        // constructor binding'de typeMismatch üretir — wrapper + null=false
        Boolean featured,

        Boolean active,

        @NotNull(message = "{validation.required}")
        @Min(value = 0, message = "{admin.validation.sortOrder}")
        Integer sortOrder,

        @NotBlank(message = "{validation.required}")
        @Size(max = 300, message = "{validation.toolong}")
        String metaDescriptionTr,

        @Size(max = 300, message = "{validation.toolong}")
        String metaDescriptionEn) {
}
