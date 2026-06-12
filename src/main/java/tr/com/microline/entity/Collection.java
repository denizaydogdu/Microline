package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Locale;

/* Tablo adı "collections": "collection" SQL'de rezerve kelime karışıklığı riski taşır. */
@Entity
@Table(name = "collections")
public class Collection extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CollectionCode code;

    @Column(name = "slug_tr", nullable = false, unique = true)
    private String slugTr;

    @Column(name = "slug_en", unique = true)
    private String slugEn;

    @Column(name = "name_tr", nullable = false)
    private String nameTr;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "description_tr", nullable = false)
    private String descriptionTr;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Transient
    public String getName(Locale locale) {
        return I18n.pick(locale, nameTr, nameEn);
    }

    @Transient
    public String getDescription(Locale locale) {
        return I18n.pick(locale, descriptionTr, descriptionEn);
    }

    public CollectionCode getCode() {
        return code;
    }

    public void setCode(CollectionCode code) {
        this.code = code;
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

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
