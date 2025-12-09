package com.library.librarymanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Borrowing servisindeki tüm endpointleri erişilebilir yap
                        // (Auth kontrolü Member Service ve Gateway seviyesinde yönetiliyor varsayıyoruz)
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}