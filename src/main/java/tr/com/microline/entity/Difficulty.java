package tr.com.microline.entity;

import java.util.Locale;

public enum Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    /**
     * CSS sınıf eki (difficulty-beginner vb.). Şablonda #strings.toLowerCase
     * KULLANILMAZ: Türkçe locale'de I → ı dönüşümü sınıf adını bozar.
     */
    public String cssSuffix() {
        return name().toLowerCase(Locale.ROOT);
    }
}
