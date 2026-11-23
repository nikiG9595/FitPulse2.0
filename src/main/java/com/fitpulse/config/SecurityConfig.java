package com.fitpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthReturnNavigation navigation, org.springframework.beans.factory.ObjectProvider<GoogleLogin> googleProvider) throws Exception {
        GoogleLogin google = googleProvider.getIfAvailable();
        if (google != null && google.isEnabled()) {
            http.oauth2Login(login -> login.clientRegistrationRepository(google.clients()).loginPage("/login")
                    .userInfoEndpoint(info -> info.oidcUserService(google::authenticate))
                    .successHandler((request, response, authentication) -> {
                        if (google.finish(request)) {
                            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/profile/edit?googleLinked"));
                        } else {
                            navigation.onAuthenticationSuccess(request, response, authentication);
                        }
                    }).failureHandler(google::failure));
        }
        return http.addFilterBefore(new ProfilePhotoUploadFilter(),
                        org.springframework.security.web.csrf.CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Match every HTTP method explicitly, independently of MVC handler matching.
                        // Keep these protected namespaces before all public/static-resource rules.
                        .requestMatchers(
                                new AntPathRequestMatcher("/meal-plans"),
                                new AntPathRequestMatcher("/meal-plans/**"),
                                new AntPathRequestMatcher("/programs"),
                                new AntPathRequestMatcher("/programs/**"),
                                new AntPathRequestMatcher("/training-sessions"),
                                new AntPathRequestMatcher("/training-sessions/**")
                        ).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/suggest-gym"), new AntPathRequestMatcher("/suggest-gym/**")).authenticated()
                        .requestMatchers("/support", "/about").permitAll()
                        .requestMatchers(HttpMethod.GET, "/terms", "/privacy", "/cookies", "/js/media-preferences.js").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/terms", "/privacy", "/cookies").permitAll()
                        .requestMatchers(HttpMethod.POST, "/gyms/*/favorite", "/gyms/*/review", "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/gyms", "/gyms/**", "/js/weekly-menu.js", "/js/program-accordion.js").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/gyms", "/gyms/**").permitAll()
                        .requestMatchers("/", "/login", "/register", "/register/google", "/js/account.js", "/css/**", "/images/**", "/exercises/**", "/js/anatomy.js", "/js/navigation.js", "/js/theme.js", "/js/language.js", "/users/**", "/trainers", "/trainers/*").permitAll()
                        .requestMatchers("/progress/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").successHandler(navigation).failureHandler((request, response, exception) -> navigation.loginFailure(request, response)).permitAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> navigation.beginAuthentication(request, response))
                        .accessDeniedHandler((request, response, exception) -> {
                            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            boolean guest = authentication == null || !authentication.isAuthenticated() || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
                            String path = request.getServletPath();
                            boolean loginRequired = path.matches("/(?:meal-plans|programs|training-sessions)(?:/.*)?") || path.equals("/suggest-gym") || path.matches("/gyms/[^/]+/(?:favorite|review)") || path.startsWith("/reviews/");
                            // Also send expired/absent-session form submissions to login; CSRF remains enforced for signed-in users.
                            if (guest && loginRequired) navigation.beginAuthentication(request, response);
                            else response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                        }))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .build();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
