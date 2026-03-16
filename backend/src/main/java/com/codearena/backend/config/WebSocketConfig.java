package com.codearena.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // This annotation enables the WebSocket server
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Why this @Override?
     * We are implementing the 'WebSocketMessageBrokerConfigurer' interface.
     * This method allows us to register STOMP "endpoints."
     * * An "endpoint" is the single HTTP URL that our client will connect to
     * to perform the WebSocket handshake.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Creates an endpoint at "/ws-connect".
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns(
                        "http://127.0.0.1:5500",  // The origin from your error message
                        "http://localhost:5174",  // Your future real frontend (from CorsConfig)
                        "http://localhost:5173",   // Your future real frontend (from CorsConfig)
                        "http://localhost:8080"    // The server itself (good to include)
                )
                .withSockJS();
    }

    /**
     * Why this @Override?
     * This method configures the "message broker" (the STOMP "post office").
     * It sets up the routes for messages.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 1. For clients SUBSCRIBING
        // This enables a simple, in-memory message broker.
        // It defines that any destination prefixed with "/topic" is a
        // "broadcast" channel. Clients subscribe to these to *receive* messages.
        // Example: /topic/room/12345
        registry.enableSimpleBroker("/topic");

        // 2. For clients SENDING
        // This defines the "inbox" for our server.
        // Any message a client *sends* to the server should be prefixed with "/app".
        // Spring will route these messages to our @MessageMapping controllers.
        // Example: /app/chat.sendMessage
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
