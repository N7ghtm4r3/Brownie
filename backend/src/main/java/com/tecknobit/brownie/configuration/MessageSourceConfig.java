package com.tecknobit.brownie.configuration;

import com.tecknobit.equinoxcore.annotations.FutureEquinoxApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
@FutureEquinoxApi(
        releaseVersion = "1.0.9",
        additionalNotes = "This will be replace the current translating system with the Mantis library"
)
public class MessageSourceConfig {

    private static final String MESSAGES_KEY = "lang/messages";

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(MESSAGES_KEY);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }

}
