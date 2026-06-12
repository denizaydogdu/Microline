package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.Locale;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "slug_tr", nullable = false, unique = true)
    private String slugTr;

    @Column(name = "slug_en", unique = true)
    private String slugEn;

    @Column(name = "name_tr", nullable = false)
    private String nameTr;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "tagline_tr", nullable = false)
    private String taglineTr;

    @Column(name = "tagline_en")
    private String taglineEn;

    /** HTML içerir; şablonlarda th:utext ile basılır. */
    @Column(name = "description_tr", nullable = false)
    private String descriptionTr;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "safety_notes_tr", nullable = false)
    private String safetyNotesTr;

    @Column(name = "safety_notes_en")
    private String safetyNotesEn;

    /** Serbest yapılı teknik özellikler (jsonb); şema v2'de değişebilsin diye kolonlara açılmadı. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specs_json")
    private String specsJson;

    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "meta_description_tr", nullable = false)
    private String metaDescriptionTr;

    @Column(name = "meta_description_en")
    private String metaDescriptionEn;

    @Transient
    public String getName(Locale locale) {
        return I18n.pick(locale, nameTr, nameEn);
    }

    @Transient
    public String getTagline(Locale locale) {
        return I18n.pick(locale, taglineTr, taglineEn);
    }

    @Transient
    public String getDescription(Locale locale) {
        return I18n.pick(locale, descriptionTr, descriptionEn);
    }

    @Transient
    public String getSafetyNotes(Locale locale) {
        return I18n.pick(locale, safetyNotesTr, safetyNotesEn);
    }

    @Transient
    public String getMetaDescription(Locale locale) {
        return I18n.pick(locale, metaDescriptionTr, metaDescriptionEn);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSlugTr() {
        return slugTr;
    }

    public void setSlugTr(String slugTr) {
        this.slugTr = slugTr;
    }

    public String getSlugEn() {
        return slugEn;
    }

    public void setSlugEn(String slugEn) {
        this.slugEn = slugEn;
    }

    public String getNameTr() {
        return nameTr;
    }

    public void setNameTr(String nameTr) {
        this.nameTr = nameTr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getTaglineTr() {
        return taglineTr;
    }

    public void setTaglineTr(String taglineTr) {
        this.taglineTr = taglineTr;
    }

    public String getTaglineEn() {
        return taglineEn;
    }

    public void setTaglineEn(String taglineEn) {
        this.taglineEn = taglineEn;
    }

    public String getDescriptionTr() {
        return descriptionTr;
    }

    public void setDescriptionTr(String descriptionTr) {
        this.descriptionTr = descriptionTr;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getSafetyNotesTr() {
        return safetyNotesTr;
    }

    public void setSafetyNotesTr(String safetyNotesTr) {
        this.safetyNotesTr = safetyNotesTr;
    }

    public String getSafetyNotesEn() {
        return safetyNotesEn;
    }

    public void setSafetyNotesEn(String safetyNotesEn) {
        this.safetyNotesEn = safetyNotesEn;
    }

    public String getSpecsJson() {
        return specsJson;
    }

    public void setSpecsJson(String specsJson) {
        this.specsJson = specsJson;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public void setCompareAtPrice(BigDecimal compareAtPrice) {
        this.compareAtPrice = compareAtPrice;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getMetaDescriptionTr() {
        return metaDescriptionTr;
    }

    public void setMetaDescriptionTr(String metaDescriptionTr) {
        this.metaDescriptionTr = metaDescriptionTr;
    }

    public String getMetaDescriptionEn() {
        return metaDescriptionEn;
    }

    public void setMetaDescriptionEn(String metaDescriptionEn) {
        this.metaDescriptionEn = metaDescriptionEn;
    }
}
