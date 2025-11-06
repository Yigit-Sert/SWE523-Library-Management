package com.library.librarymanagement.config;

import com.library.librarymanagement.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // --- ADMIN-ONLY PERMISSIONS ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- SHARED PERMISSIONS for PERSONNEL and ADMIN ---
                        .requestMatchers(
                                "/api/members/**",          // Full CRUD for library member records.
                                "/api/borrowings/**"        // Issuing and returning books.
                        ).hasAnyRole("PERSONNEL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN")   // Create books.
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN")    // Update books.
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN") // Delete books.
                        .requestMatchers(HttpMethod.GET, "/api/requests").hasAnyRole("PERSONNEL", "ADMIN")   // View ALL book requests.
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/approve", "/api/requests/*/reject").hasAnyRole("PERSONNEL", "ADMIN") // Approve or reject requests.

                        // --- MEMBER-ONLY PERMISSIONS ---
                        .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("MEMBER")                 // Create a new book request.
                        .requestMatchers(HttpMethod.GET, "/api/requests/my-requests").hasRole("MEMBER")      // View their own requests.

                        // --- PUBLIC PERMISSIONS ---
                        .requestMatchers(HttpMethod.GET, "/api/books/**", "/api/users/profile/picture/**").permitAll()
                        .requestMatchers("/", "/index.html", "/style.css", "/app.js", "/favicon.ico").permitAll()

                        // --- CATCH-ALL ---
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 ->
                        oauth2
                                .userInfoEndpoint(userInfo ->
                                        userInfo.oidcUserService(customOidcUserService)
                                )
                                .successHandler(oauth2LoginSuccessHandler)
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}