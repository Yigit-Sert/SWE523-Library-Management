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
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // --- Public Access & Static Resources ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/books/**",
                                "/api/users/profile/picture/**",
                                "/api/users/search",
                                "/api/users/{id}",
                                "/api/members/{id}",
                                "/", "/index.html", "/style.css", "/app.js", "/favicon.ico"
                        ).permitAll()

                        // --- Role Based Access Control ---
                        // Admin Only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Personnel & Admin Shared
                        .requestMatchers(
                                "/api/members/**",
                                "/api/borrowings/**"
                        ).hasAnyRole("PERSONNEL", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("PERSONNEL", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/requests").hasAnyRole("PERSONNEL", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/approve", "/api/requests/*/reject").hasAnyRole("PERSONNEL", "ADMIN")

                        // Member Only
                        .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.GET, "/api/requests/my-requests").hasRole("MEMBER")

                        // --- Default: Authenticated ---
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oauth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(appBaseUrl + "/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }
}