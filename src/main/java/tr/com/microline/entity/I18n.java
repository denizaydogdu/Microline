package tr.com.microline.entity;

import java.util.Locale;

/**
 * Türkçe-öncelikli alan seçimi: EN sadece dil İngilizce VE çeviri mevcutsa
 * döner; aksi halde TR (fallback). Entity convenience getter'ları bunu kullanır.
 */
final class I18n {

    private I18n() {
    }

    static String pick(Locale locale, String tr, String en) {
        if (locale != null && "en".equals(locale.getLanguage()) && en != null && !en.isBlank()) {
            return en;
        }
        return tr;
    }
}
