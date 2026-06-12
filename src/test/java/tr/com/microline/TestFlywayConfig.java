package tr.com.microline;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * microline_test kalıcı bir yerel DB'dir (Testcontainers yok): her context
 * açılışında clean + migrate yapılmazsa önceki koşunun şeması/verisi sızar.
 * clean, application-test.yml'deki clean-disabled:false sayesinde çalışır.
 */
@TestConfiguration
public class TestFlywayConfig {

    @Bean
    FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
