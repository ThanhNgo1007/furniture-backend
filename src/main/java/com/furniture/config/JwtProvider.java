package com.furniture.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets; // Import thêm
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretString; // Đổi tên biến để tránh nhầm lẫn

    // Hàm helper để chuyển đổi String thành SecretKey
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Tạo Access Token (15 phút)
    public String generateAccessToken(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String roles = populateAuthorities(authorities);

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 900000)) // 15 phút
                .claim("email", auth.getName())
                .claim("authorities", roles)
                // SỬA LỖI: Dùng getSecretKey() thay vì truyền String trực tiếp
                .signWith(getSecretKey())
                .compact();
    }

    // 2. Tạo Refresh Token (7 ngày)
    public String generateRefreshToken(Authentication auth) {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 604800000)) // 7 ngày
                .claim("email", auth.getName())
                .claim("type", "refresh")
                // SỬA LỖI: Dùng getSecretKey()
                .signWith(getSecretKey())
                .compact();
    }

    public String getEmailFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        // SỬA LỖI: Dùng getSecretKey()
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        return String.valueOf(claims.get("email"));
    }

    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",", auths);
    }
}