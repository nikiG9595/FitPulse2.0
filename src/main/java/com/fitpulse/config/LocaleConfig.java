package com.fitpulse.config;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class LocaleConfig {
    @Bean
    LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("fitpulse-language") {
            @Override
            protected Locale parseLocaleValue(String value) {
                return "en".equals(value) ? Locale.ENGLISH : Locale.forLanguageTag("bg");
            }
        };
        resolver.setDefaultLocale(Locale.forLanguageTag("bg"));
        resolver.setRejectInvalidCookies(false);
        return resolver;
    }
}
