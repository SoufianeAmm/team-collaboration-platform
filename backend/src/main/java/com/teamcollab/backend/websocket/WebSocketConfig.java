package com.teamcollab.backend.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Wires up STOMP-over-WebSocket, separate from the REST API's request/response model.
 * REST (see the controller package) handles normal CRUD; this handles the subset of events
 * that genuinely benefit from being pushed live (chat today, task/notification events in
 * later phases) instead of the client having to poll for them.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor authInterceptor;
    private final String corsAllowedOrigin;

    public WebSocketConfig(StompAuthChannelInterceptor authInterceptor,
                            @Value("${cors.allowed-origin}") String corsAllowedOrigin) {
        this.authInterceptor = authInterceptor;
        this.corsAllowedOrigin = corsAllowedOrigin;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Plain WebSocket, no SockJS fallback - every target browser here supports it natively,
        // and skipping SockJS means the frontend only needs @stomp/stompjs, not an extra library.
        registry.addEndpoint("/ws").setAllowedOrigins(corsAllowedOrigin);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client SENDs go to destinations starting with /app, routed to an @MessageMapping method.
        registry.setApplicationDestinationPrefixes("/app");
        // The broker owns /topic (project chat/task broadcasts) and /queue destinations and fans
        // out messages to every subscriber itself - our code never touches subscriber lists
        // directly. /queue matters even though nothing SENDs there directly: per-user
        // notifications (convertAndSendToUser) resolve internally to a /queue/... destination,
        // and the broker silently drops anything outside the prefixes registered here.
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
