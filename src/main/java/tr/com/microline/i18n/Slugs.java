package tr.com.microline.i18n;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** URL slug üretimi: "Yeni Test Şablonu" → "yeni-test-sablonu". */
public final class Slugs {

    /**
     * Türkçe harfler ASCII karşılığına ÖNCE çevrilir: NFD ayrıştırması
     * ı/İ'yi doğru ele almaz (ı'nın combining mark'ı yoktur) ve İ→i
     * dönüşümü locale'e bağlıdır.
     */
    private static final Map<Character, Character> TURKISH_MAP = Map.ofEntries(
            Map.entry('ı', 'i'), Map.entry('İ', 'i'),
            Map.entry('ş', 's'), Map.entry('Ş', 's'),
            Map.entry('ğ', 'g'), Map.entry('Ğ', 'g'),
            Map.entry('ç', 'c'), Map.entry('Ç', 'c'),
            Map.entry('ö', 'o'), Map.entry('Ö', 'o'),
            Map.entry('ü', 'u'), Map.entry('Ü', 'u'));

    private Slugs() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        StringBuilder mapped = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            mapped.append(TURKISH_MAP.getOrDefault(c, c));
        }
        // Locale.ROOT şart: TR locale'de "I".toLowerCase() → "ı" olur ve
        // [a-z] dışına düşer (klasik Turkish-I tuzağı).
        String lowered = mapped.toString().toLowerCase(Locale.ROOT);
        // Kalan aksanlı harfler (é, â...) NFD ile taban harfe indirilir
        String stripped = Normalizer.normalize(lowered, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
