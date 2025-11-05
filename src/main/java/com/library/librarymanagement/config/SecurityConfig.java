package com.library.librarymanagement.config;

import com.library.librarymanagement.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.authentication.AuthenticationSuccessHandler; // Gerekirse ekleyin

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/members/**", "/api/borrowings/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/profile/picture/**").permitAll()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers("/api/books/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 ->
                        oauth2
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(customOAuth2UserService)
                                )
                                .successHandler(oauth2LoginSuccessHandler)
                );
        return http.build();
    }
}