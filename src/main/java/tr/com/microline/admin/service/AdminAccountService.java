package tr.com.microline.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.microline.entity.AdminUser;
import tr.com.microline.repository.AdminUserRepository;

@Service
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Mevcut şifre doğruysa yenisini BCrypt'leyip yazar.
     *
     * @return false: mevcut şifre tutmadı (alan hatası olarak gösterilir)
     */
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        AdminUser admin = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Oturumdaki admin DB'de yok: " + username));
        if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
            return false;
        }
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        return true;
    }
}
