package tr.com.microline.admin.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import tr.com.microline.entity.AdminUser;
import tr.com.microline.repository.AdminUserRepository;

/**
 * İlk admin'i migration yerine açılışta oluşturur: git'e şifre hash'i girmez.
 * Yalnızca tablo BOŞKEN koşar (idempotent — sonraki açılışlar dokunmaz).
 * Şifre verilmemişse rastgele üretilir ve bir kereliğine WARN loglanır;
 * kullanıcı girişten sonra /admin/sifre ile değiştirir.
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialUsername;
    private final String initialPassword;

    public AdminUserInitializer(AdminUserRepository adminUserRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${microline.admin.initial-username:admin}") String initialUsername,
                                @Value("${microline.admin.initial-password:}") String initialPassword) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialUsername = initialUsername;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        String password = initialPassword;
        if (password == null || password.isBlank()) {
            password = UUID.randomUUID().toString();
            log.warn("İlk admin şifresi: {} (kullanıcı: {}) — girişten sonra /admin/sifre ile değiştirin.",
                    password, initialUsername);
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(initialUsername);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setDisplayName("Yönetici");
        admin.setEnabled(true);
        adminUserRepository.save(admin);
        log.info("İlk admin kullanıcısı oluşturuldu: {}", initialUsername);
    }
}
