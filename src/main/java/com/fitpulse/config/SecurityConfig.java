package com.fitpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,AuthReturnNavigation navigation) throws Exception {
        return http
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
                        .requestMatchers("/suggest-gym").authenticated()
                        .requestMatchers(HttpMethod.POST, "/gyms/*/favorite", "/gyms/*/review", "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/gyms", "/gyms/**", "/js/weekly-menu.js", "/js/program-accordion.js").permitAll()
                        .requestMatchers(HttpMethod.HEAD,"/gyms", "/gyms/**").permitAll()
                        .requestMatchers("/", "/login", "/register", "/css/**", "/images/**", "/exercises/**", "/js/anatomy.js", "/js/navigation.js", "/users/**", "/trainers", "/trainers/*").permitAll()
                        .requestMatchers("/progress/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").successHandler(navigation).failureHandler((request,response,exception)->navigation.loginFailure(request,response)).permitAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request,response,exception)->navigation.beginAuthentication(request,response))
                        .accessDeniedHandler((request,response,exception)->{
                            var authentication=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            boolean guest=authentication==null||!authentication.isAuthenticated()||authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
                            String path=request.getServletPath();
                            boolean loginRequired=path.matches("/(?:meal-plans|programs|training-sessions)(?:/.*)?")||path.equals("/suggest-gym")||path.matches("/gyms/[^/]+/(?:favorite|review)")||path.startsWith("/reviews/");
                            // Also send expired/absent-session form submissions to login; CSRF remains enforced for signed-in users.
                            if(guest&&loginRequired)navigation.beginAuthentication(request,response);
                            else response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                        }))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
