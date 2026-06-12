package tr.com.microline.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_requests")
public class OrderRequest extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    /** Sepet satırları; talep ile birlikte cascade ile yazılır. */
    @OneToMany(mappedBy = "orderRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderRequestItem> items = new ArrayList<>();

    @Column
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;

    /** KVKK açık rıza olmadan form kaydedilmez — DB de NOT NULL ile zorlar. */
    @Column(name = "kvkk_consent", nullable = false)
    private boolean kvkkConsent;

    @Column(nullable = false, length = 2)
    private String locale;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<OrderRequestItem> getItems() {
        return items;
    }

    public void addItem(OrderRequestItem item) {
        item.setOrderRequest(this);
        items.add(item);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public void setStatus(InquiryStatus status) {
        this.status = status;
    }

    public boolean isKvkkConsent() {
        return kvkkConsent;
    }

    public void setKvkkConsent(boolean kvkkConsent) {
        this.kvkkConsent = kvkkConsent;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
}
