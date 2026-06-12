package tr.com.microline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tr.com.microline.entity.CollectionCode;
import tr.com.microline.repository.CollectionRepository;
import tr.com.microline.repository.ProductRepository;

/**
 * Context-load + Flyway/şema doğrulama testi. ddl-auto=validate olduğu için
 * context'in ayağa kalkması, entity'lerin V1 şemasıyla birebir eşleştiğini kanıtlar.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
class MicrolineApplicationTests {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void flywaySeedIsQueryableThroughRepositories() {
        assertThat(collectionRepository.count()).isEqualTo(4);
        assertThat(collectionRepository.findByCode(CollectionCode.CUTTING_TOOLS)).isPresent();
        assertThat(productRepository.findBySlugTrAndActiveTrue("microline-kesim-makinesi"))
                .hasValueSatisfying(p -> assertThat(p.isFeatured()).isTrue());
    }
}
