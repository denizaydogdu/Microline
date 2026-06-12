package tr.com.microline.dto;

/** Yorumlar sayfasının özet başlığı (ortalama, adet, 5 yıldız oranı). */
public record ReviewStats(double average, int count, int fiveStarPercent) {

    public static final ReviewStats EMPTY = new ReviewStats(0, 0, 0);

    /** Yıldız ikonları tam sayı ister; ortalamanın yuvarlanmış hali. */
    public int rounded() {
        return (int) Math.round(average);
    }
}
