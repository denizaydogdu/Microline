package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "newsletter_subscribers")
public class NewsletterSubscriber extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 2)
    private String locale;

    @Column(name = "kvkk_consent_at", nullable = false)
    private Instant kvkkConsentAt;

    @Column(name = "unsubscribe_token", nullable = false, unique = true)
    private UUID unsubscribeToken;

    /** Null = abonelik aktif; satır silinmez ki rıza kaydı (KVKK) korunmuş olsun. */
    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Instant getKvkkConsentAt() {
        return kvkkConsentAt;
    }

    public void setKvkkConsentAt(Instant kvkkConsentAt) {
        this.kvkkConsentAt = kvkkConsentAt;
    }

    public UUID getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public void setUnsubscribeToken(UUID unsubscribeToken) {
        this.unsubscribeToken = unsubscribeToken;
    }

    public Instant getUnsubscribedAt() {
        return unsubscribedAt;
    }

    public void setUnsubscribedAt(Instant unsubscribedAt) {
        this.unsubscribedAt = unsubscribedAt;
    }
}
