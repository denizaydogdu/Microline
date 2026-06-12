package tr.com.microline.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.microline.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);
}
