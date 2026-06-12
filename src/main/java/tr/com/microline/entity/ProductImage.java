package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Locale;

@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;

    @Column(name = "alt_tr", nullable = false)
    private String altTr;

    @Column(name = "alt_en")
    private String altEn;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Transient
    public String getAlt(Locale locale) {
        return I18n.pick(locale, altTr, altEn);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAltTr() {
        return altTr;
    }

    public void setAltTr(String altTr) {
        this.altTr = altTr;
    }

    public String getAltEn() {
        return altEn;
    }

    public void setAltEn(String altEn) {
        this.altEn = altEn;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
