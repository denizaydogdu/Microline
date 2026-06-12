package tr.com.microline.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tr.com.microline.i18n.PathLocaleResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MessageSource messageSource;
    private final Path uploadsRoot;

    public WebConfig(MessageSource messageSource,
                     @Value("${microline.uploads-dir}") String uploadsDir) {
        this.messageSource = messageSource;
        this.uploadsRoot = Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    /**
     * Admin'in yüklediği görseller deploy artefaktının dışındaki diskten
     * servis edilir. Classpath'teki /img/ seed görselleri varsayılan static
     * handler'da kalır — bu mapping onlara dokunmaz. Sonundaki "/" zorunlu:
     * yoksa Spring location'ı dizin değil dosya sanır ve hiçbir şey çözülmez.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadsRoot.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }

    /** Bean adı "localeResolver" olmak zorunda; DispatcherServlet isimle arar. */
    @Bean
    public LocaleResolver localeResolver() {
        return new PathLocaleResolver();
    }

    /**
     * Bean Validation mesajları ({validation.required} vb.) Hibernate'in
     * ValidationMessages.properties'inden değil, kendi messages bundle'ımızdan
     * dile göre çözülsün diye validator MessageSource'a bağlanır.
     */
    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Override
    public Validator getValidator() {
        return validatorFactory();
    }
}
