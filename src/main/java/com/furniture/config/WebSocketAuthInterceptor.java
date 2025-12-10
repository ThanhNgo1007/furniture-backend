package com.furniture.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    @Nullable
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("[WebSocket Auth] CONNECT frame received");
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            System.out.println("[WebSocket Auth] Authorization headers: " + authHeaders);
            
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.getFirst();
                System.out.println("[WebSocket Auth] Full Authorization header: " + authHeader);
                
                if (!authHeader.startsWith("Bearer ")) {
                    System.out.println("[WebSocket Auth] ERROR: Authorization header does not start with 'Bearer '");
                    return null; // Reject connection
                }
                
                String jwt = authHeader.replace("Bearer ", "");
                System.out.println("[WebSocket Auth] JWT token extracted (first 20 chars): " + 
                    (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt));
                
                try {
                    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(jwt)
                            .getBody();

                    // Email is stored in custom "email" claim, not in subject
                    String email = claims.get("email", String.class);

                    System.out.println("[WebSocket Auth] ✅ JWT valid - User email: " + email);
                    
                    // Set user principal (you can extend this to fetch full user details)
                    accessor.setUser(new UsernamePasswordAuthenticationToken(email, null, List.of()));

                } catch (Exception e) {
                    System.out.println("[WebSocket Auth] ❌ JWT validation failed: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.out.println("[WebSocket Auth] ❌ No Authorization header found in CONNECT frame");
                return null; // Reject if no auth header
            }
        }

        return message;
    }
}
