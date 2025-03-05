package com.tecknobit.brownie.configuration;

import com.tecknobit.equinoxcore.annotations.FutureEquinoxApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import static java.util.Locale.ENGLISH;

@Configuration
@FutureEquinoxApi(
        protoBehavior = "Fix the LocaleChangeInterceptor behavior ",
        releaseVersion = "1.0.9",
        additionalNotes = "This will be replace the current translating system with the Mantis library"
)
public class MessageSourceConfig implements WebMvcConfigurer {

    private static final String MESSAGES_KEY = "lang/messages";

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(MESSAGES_KEY);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(ENGLISH);
        LocaleContextHolder.setLocale(ENGLISH);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(ENGLISH);
        return resolver;
    }

    /*@Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LANGUAGE_KEY);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }*/

}
