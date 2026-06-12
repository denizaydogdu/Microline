package tr.com.microline.i18n;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Şablonlardan ${@prices.format(...)} ile çağrılan fiyat biçimleyici.
 * TR: "4.990 ₺" (sembol sonda), EN: "₺4,990" (sembol başta). Kuruşsuz
 * fiyatlarda ondalık gösterilmez; kuruşlu fiyatlarda daima 2 hane.
 */
@Component("prices")
public class Prices {

    public String format(BigDecimal amount, Locale locale) {
        if (amount == null) {
            return "";
        }
        boolean english = Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        NumberFormat nf = NumberFormat.getNumberInstance(english ? Locale.ENGLISH : PathLocaleResolver.TURKISH);
        boolean wholeAmount = amount.stripTrailingZeros().scale() <= 0;
        nf.setMinimumFractionDigits(wholeAmount ? 0 : 2);
        nf.setMaximumFractionDigits(wholeAmount ? 0 : 2);
        String number = nf.format(amount);
        return english ? "₺" + number : number + " ₺";
    }
}
