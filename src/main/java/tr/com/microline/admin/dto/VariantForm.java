package tr.com.microline.admin.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Varyant ekleme/düzenleme mini-formu. priceDelta negatif olabilir (indirimli paket). */
public record VariantForm(
        @NotBlank(message = "{validation.required}")
        @Size(max = 40, message = "{validation.toolong}")
        String sku,

        @NotBlank(message = "{validation.required}")
        @Size(max = 200, message = "{validation.toolong}")
        String nameTr,

        @Size(max = 200, message = "{validation.toolong}")
        String nameEn,

        @NotNull(message = "{validation.required}")
        @Digits(integer = 8, fraction = 2, message = "{admin.validation.price}")
        BigDecimal priceDelta,

        // Wrapper: işaretsiz checkbox/eksik parametre null gelir (= pasif)
        Boolean active,

        @NotNull(message = "{validation.required}")
        @Min(value = 0, message = "{admin.validation.sortOrder}")
        Integer sortOrder) {
}
