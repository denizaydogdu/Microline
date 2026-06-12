package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Locale;

/**
 * Yorumlar tek dilli orijinallerdir (çeviri çifti yok) — sayfada `locale`
 * kolonuyla mevcut dile göre filtrelenir. authorTitle istisnadır (kısa unvan, çevrilebilir).
 */
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    /** Null = ürüne bağlı olmayan genel marka yorumu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_title_tr")
    private String authorTitleTr;

    @Column(name = "author_title_en")
    private String authorTitleEn;

    @Column(nullable = false)
    private short rating;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false, length = 2)
    private String locale;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private boolean approved;

    /** Yorumun geldiği kanal (ör. "email", "form"); serbest metin. */
    @Column
    private String source;

    @Transient
    public String getAuthorTitle(Locale loc) {
        return I18n.pick(loc, authorTitleTr, authorTitleEn);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorTitleTr() {
        return authorTitleTr;
    }

    public void setAuthorTitleTr(String authorTitleTr) {
        this.authorTitleTr = authorTitleTr;
    }

    public String getAuthorTitleEn() {
        return authorTitleEn;
    }

    public void setAuthorTitleEn(String authorTitleEn) {
        this.authorTitleEn = authorTitleEn;
    }

    public short getRating() {
        return rating;
    }

    public void setRating(short rating) {
        this.rating = rating;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
