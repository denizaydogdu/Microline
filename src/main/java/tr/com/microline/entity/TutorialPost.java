package tr.com.microline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "tutorial_posts")
public class TutorialPost extends BaseEntity {

    @Column(name = "slug_tr", nullable = false, unique = true)
    private String slugTr;

    @Column(name = "slug_en", unique = true)
    private String slugEn;

    @Column(name = "title_tr", nullable = false)
    private String titleTr;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "excerpt_tr", nullable = false)
    private String excerptTr;

    @Column(name = "excerpt_en")
    private String excerptEn;

    /** HTML içerir; şablonlarda th:utext ile basılır. */
    @Column(name = "body_tr", nullable = false)
    private String bodyTr;

    @Column(name = "body_en")
    private String bodyEn;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Transient
    public String getTitle(Locale locale) {
        return I18n.pick(locale, titleTr, titleEn);
    }

    @Transient
    public String getExcerpt(Locale locale) {
        return I18n.pick(locale, excerptTr, excerptEn);
    }

    @Transient
    public String getBody(Locale locale) {
        return I18n.pick(locale, bodyTr, bodyEn);
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

    public String getExcerptTr() {
        return excerptTr;
    }

    public void setExcerptTr(String excerptTr) {
        this.excerptTr = excerptTr;
    }

    public String getExcerptEn() {
        return excerptEn;
    }

    public void setExcerptEn(String excerptEn) {
        this.excerptEn = excerptEn;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
