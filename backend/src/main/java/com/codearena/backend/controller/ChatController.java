package com.codearena.backend.controller;

import com.codearena.backend.dto.ChatMessageDTO;
import com.codearena.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable; // ✅ Added Import
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.security.Principal;
import java.util.Date; // ✅ Added Import
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ Existing Chat Message Handler
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        try {
            String authenticatedUsername = principal.getName();
            log.debug("Chat message received from authenticated user: {}", authenticatedUsername);

            if (chatMessageDTO.getContent() == null || chatMessageDTO.getContent().trim().isEmpty()) {
                sendErrorToUser(principal, "Message content cannot be empty");
                return;
            }

            if (chatMessageDTO.getRoomCode() == 0) {
                sendErrorToUser(principal, "Room code is required");
                return;
            }

            // Set type to CHAT for normal messages
            chatMessageDTO.setType("CHAT");

            ChatMessageDTO savedDto = chatService.saveMessage(chatMessageDTO, authenticatedUsername);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + savedDto.getRoomCode(),
                    savedDto
            );

        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            sendErrorToUser(principal, "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * ✅ NEW: Typing Indicator Endpoint
     * Frontend sends to: /app/chat.typing/{roomCode}
     * We broadcast to: /topic/room/{roomCode}
     */
    @MessageMapping("/chat.typing/{roomCode}")
    public void notifyTyping(@DestinationVariable int roomCode, @Payload ChatMessageDTO message, Principal principal) {
        try {
            // 1. Securely set the sender (don't trust the DTO)
            String authenticatedUsername = principal.getName();
            message.setSenderUsername(authenticatedUsername);

            // 2. Set strict fields for typing event
            message.setRoomCode(roomCode);
            message.setType("TYPING");
            message.setContent("Typing..."); // Optional placeholder
            message.setTimestamp(new Date());

            // 3. Broadcast directly (DO NOT SAVE TO DB)
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, message);

        } catch (Exception e) {
            log.warn("Error sending typing indicator: {}", e.getMessage());
        }
    }

    /**
     * ✅ REST endpoint to get chat history
     */
    @GetMapping("/api/v1/chat/history/{roomCode}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable int roomCode) {
        try {
            return ResponseEntity.ok(chatService.getMessages(roomCode));
        } catch (Exception e) {
            log.error("Error fetching chat history: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void sendErrorToUser(Principal principal, String errorMessage) {
        try {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new ErrorMessage(errorMessage, System.currentTimeMillis())
            );
        } catch (Exception e) {
            log.error("Failed to send error message: {}", e.getMessage());
        }
    }

    private record ErrorMessage(String error, long timestamp) {}
}