package com.furniture.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AppConfig {
    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ===== 1. PUBLIC ENDPOINTS (Không cần authentication) =====

                        // Auth endpoints - QUAN TRỌNG: Thêm /auth/refresh
                        .requestMatchers(
                                "/auth/sent/login-signup-otp",
                                "/auth/signing",
                                "/auth/signup",
                                "/auth/refresh"  // ⭐ THÊM ENDPOINT REFRESH TOKEN
                        ).permitAll()

                        // Seller public endpoints
                        .requestMatchers(
                                "/sellers/login",
                                "/sellers/account-status",
                                "/sellers/verify/**" // Verify email cũng cần public
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/sellers").permitAll() // Đăng ký seller public

                        // Payment & Product reviews
                        .requestMatchers("/api/payment/**").permitAll()
                        .requestMatchers("/api/products/*/reviews").permitAll()

                        // Public API endpoints - Allow anonymous access
                        .requestMatchers("/api/deals").permitAll()
                        .requestMatchers("/api/home-categories").permitAll()
                        .requestMatchers("/api/products").permitAll()
                        .requestMatchers("/api/products/**").permitAll()

                        // ===== 2. PROTECTED ENDPOINTS (Yêu cầu authentication) =====

                        // Tất cả API khác của customer
                        .requestMatchers("/api/**").authenticated()

                        // Tất cả API của seller (trừ login đã permit ở trên)
                        .requestMatchers("/sellers/**").authenticated()

                        // ===== 3. CÁC REQUEST KHÁC =====
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtTokenValidator(secretKey), BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // CORS Configuration
    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration cfg = new CorsConfiguration();

            // Allowed origins
            cfg.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:4000"
            ));

            // Allowed methods
            cfg.setAllowedMethods(Collections.singletonList("*"));

            // Credentials
            cfg.setAllowCredentials(true);

            // Headers
            cfg.setAllowedHeaders(Collections.singletonList("*"));
            cfg.setExposedHeaders(Arrays.asList("Authorization"));

            // Max age
            cfg.setMaxAge(3600L);

            return cfg;
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}