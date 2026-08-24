package com.fitpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**").permitAll()
                        .requestMatchers("/progress/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/classes/create", "/classes/*/edit", "/classes/*/delete", "/memberships/create", "/memberships/*/edit", "/memberships/*/delete", "/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
