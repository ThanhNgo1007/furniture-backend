package com.furniture.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // CORS_ALLOWED_ORIGINS: comma-separated list of allowed origins
    // Example: http://localhost:5173,https://furniture-frontend.pages.dev
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://furniture-frontend.nhthanh1007.workers.dev}")
    private String allowedOrigins;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Allowed origins - parse from environment variable
        String[] origins = allowedOrigins.split(",");
        cfg.setAllowedOrigins(Arrays.asList(origins));

        // Allowed methods
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Credentials
        cfg.setAllowCredentials(true);

        // Headers
        cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        cfg.setExposedHeaders(List.of("Authorization"));

        // Max age (1 hour)
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
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