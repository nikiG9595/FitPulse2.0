package com.fitpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,AuthReturnNavigation navigation) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/gyms/*/favorite", "/gyms/*/review", "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/programs","/meal-plans","/js/weekly-menu.js","/js/program-accordion.js").permitAll()
                        .requestMatchers(new org.springframework.security.web.util.matcher.RegexRequestMatcher("/(?:programs|meal-plans)/[0-9]+(?:\\?.*)?","GET")).permitAll()
                        .requestMatchers("/", "/login", "/register", "/css/**", "/images/**", "/exercises/**", "/js/anatomy.js", "/js/navigation.js", "/gyms/**", "/suggest-gym", "/users/**", "/trainers", "/trainers/*").permitAll()
                        .requestMatchers("/progress/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").successHandler(navigation).failureHandler((request,response,exception)->navigation.loginFailure(request,response)).permitAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request,response,exception)->navigation.beginAuthentication(request,response)))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
