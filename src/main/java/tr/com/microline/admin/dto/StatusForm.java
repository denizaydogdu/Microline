package tr.com.microline.admin.dto;

import jakarta.validation.constraints.NotNull;
import tr.com.microline.entity.InquiryStatus;

/**
 * Talep/mesaj durum geçişi formu. Geçersiz enum string'i binding'de
 * typeMismatch üretir — controller bunu flash uyarısına çevirir,
 * durum DEĞİŞMEZ.
 */
public record StatusForm(@NotNull InquiryStatus status) {
}
