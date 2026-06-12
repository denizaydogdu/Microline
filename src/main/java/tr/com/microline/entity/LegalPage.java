package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Locale;

/** bodyEn null ise sayfa TR gövdeyle + "içerik İngilizce mevcut değil" bandıyla render edilir. */
@Entity
@Table(name = "legal_pages")
public class LegalPage extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "slug_tr", nullable = false, unique = true)
    private String slugTr;

    @Column(name = "slug_en", unique = true)
    private String slugEn;

    @Column(name = "title_tr", nullable = false)
    private String titleTr;

    @Column(name = "title_en")
    private String titleEn;

    /** HTML içerir; şablonlarda th:utext ile basılır. */
    @Column(name = "body_tr", nullable = false)
    private String bodyTr;

    @Column(name = "body_en")
    private String bodyEn;

    @Transient
    public String getTitle(Locale locale) {
        return I18n.pick(locale, titleTr, titleEn);
    }

    @Transient
    public String getBody(Locale locale) {
        return I18n.pick(locale, bodyTr, bodyEn);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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

    public String getTitleTr() {
        return titleTr;
    }

    public void setTitleTr(String titleTr) {
        this.titleTr = titleTr;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getBodyTr() {
        return bodyTr;
    }

    public void setBodyTr(String bodyTr) {
        this.bodyTr = bodyTr;
    }

    public String getBodyEn() {
        return bodyEn;
    }

    public void setBodyEn(String bodyEn) {
        this.bodyEn = bodyEn;
    }
}
