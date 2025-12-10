package com.furniture.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        System.out.println("[WebSocket Handshake] Intercepting handshake");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            System.out.println("[WebSocket Handshake] Token from query: " + 
                (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

            if (token != null && !token.isEmpty()) {
                // Store token in attributes to be used by STOMP interceptor
                attributes.put("token", token);
                System.out.println("[WebSocket Handshake] ✅ Token stored in attributes");
                return true;
            } else {
                System.out.println("[WebSocket Handshake] ❌ No token found in query params");
                return false; // Reject handshake
            }
        }

        System.out.println("[WebSocket Handshake] ❌ Not a ServletServerHttpRequest");
        return false;
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }
}
