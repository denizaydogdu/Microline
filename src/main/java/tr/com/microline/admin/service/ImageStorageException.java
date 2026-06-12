package tr.com.microline.admin.service;

/**
 * Görsel yükleme/doğrulama hatası. Controller'lar messageKey ile
 * BindingResult alan hatasına çevirir (Phase C); getMessage() Türkçe
 * fallback metnidir (log + anahtar henüz bundle'da yoksa).
 */
public class ImageStorageException extends RuntimeException {

    private final String messageKey;

    public ImageStorageException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

    public ImageStorageException(String messageKey, String message, Throwable cause) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
