package tr.com.microline.i18n;

import java.util.Locale;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * URL'in ilk path segmenti dilin tek doğruluk kaynağıdır (cookie/session yok):
 * /en/... → İngilizce, diğer her şey → Türkçe fallback.
 */
public class PathLocaleResolver implements LocaleResolver {

    public static final Locale TURKISH = Locale.of("tr");

    /** Hata dispatch'inde container orijinal URI'yi bu attribute'ta taşır. */
    private static final String ERROR_REQUEST_URI = "jakarta.servlet.error.request_uri";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Hata forward'ında getRequestURI() "/error" döner; İngilizce sayfada
        // alınan 404/500'ün İngilizce kalması için orijinal URI'ye bakılır.
        Object errorUri = request.getAttribute(ERROR_REQUEST_URI);
        String uri = errorUri instanceof String s ? s : request.getRequestURI();
        if ("/en".equals(uri) || uri.startsWith("/en/")) {
            return Locale.ENGLISH;
        }
        return TURKISH;
    }

    @Override
    public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
        throw new UnsupportedOperationException("Locale is derived from the URL path and cannot be changed");
    }
}
