package tr.com.microline.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tr.com.microline.i18n.PathLocaleResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MessageSource messageSource;

    public WebConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
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
