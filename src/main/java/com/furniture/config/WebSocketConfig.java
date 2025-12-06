package com.furniture.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for broadcasting messages
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for messages sent from client to server
        config.setApplicationDestinationPrefixes("/app");
        
        // Set prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Configure CORS for development
                .withSockJS();  // Enable SockJS fallback for browsers that don't support WebSocket
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Add authentication interceptor for WebSocket connections
        registration.interceptors(webSocketAuthInterceptor);
    }
}
