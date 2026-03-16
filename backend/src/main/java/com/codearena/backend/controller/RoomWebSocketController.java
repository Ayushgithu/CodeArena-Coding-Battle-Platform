package com.codearena.backend.controller;

import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RoomWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * ✅ WebSocket endpoint to start a room match
     * Security: Validates user is room creator before starting
     */
    @MessageMapping("/room/start")
    public void startMatch(@Payload Map<String, String> payload, Principal principal) {
        try {
            // ✅ Security: Get authenticated user from Principal
            String username = principal.getName();
            log.info("WebSocket start match request from user: {}", username);

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Parse and validate room code
            String roomCodeStr = payload.get("roomCode");
            if (roomCodeStr == null || roomCodeStr.isBlank()) {
                sendErrorToUser(principal, "Room code is required");
                return;
            }

            int roomCode;
            try {
                roomCode = Integer.parseInt(roomCodeStr);
            } catch (NumberFormatException e) {
                sendErrorToUser(principal, "Invalid room code format");
                return;
            }

            // ✅ Security: Validate room exists and user has permission
            Room room = roomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new BadRequestException("Room not found"));

            // ✅ Security: Only room creator can start the match
            if (!room.getMadeBy().getId().equals(currentUser.getId())) {
                sendErrorToUser(principal, "Only the room creator can start the match");
                return;
            }

            // ✅ Security: Validate room has both players
            if (room.getJoinBy() == null) {
                sendErrorToUser(principal, "Cannot start - waiting for second player");
                return;
            }

            log.info("Starting room {} via WebSocket by {}", roomCode, username);

            // Start the room (service handles additional validation)
            roomService.startRoom(roomCode);

            // ✅ Send success notification to both players in the room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode + "/status",
                    Map.of(
                            "event", "START_MATCH",
                            "message", "Match started successfully",
                            "startedBy", username,
                            "timestamp", System.currentTimeMillis()
                    )
            );

            log.info("Match started successfully for room: {}", roomCode);

        } catch (Exception e) {
            log.error("Error starting match via WebSocket: {}", e.getMessage(), e);
            sendErrorToUser(principal, "Failed to start match: " + e.getMessage());
        }
    }

    /**
     * ✅ WebSocket endpoint to send ready status
     */
    @MessageMapping("/room/ready")
    public void playerReady(@Payload Map<String, String> payload, Principal principal) {
        try {
            String username = principal.getName();
            String roomCodeStr = payload.get("roomCode");

            if (roomCodeStr == null) {
                sendErrorToUser(principal, "Room code is required");
                return;
            }

            int roomCode = Integer.parseInt(roomCodeStr);

            // ✅ Security: Validate user is participant
            Room room = roomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new BadRequestException("Room not found"));

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            boolean isParticipant = room.getMadeBy().getId().equals(currentUser.getId()) ||
                    (room.getJoinBy() != null && room.getJoinBy().getId().equals(currentUser.getId()));

            if (!isParticipant) {
                sendErrorToUser(principal, "You are not a participant in this room");
                return;
            }

            // Broadcast ready status to room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode + "/status",
                    Map.of(
                            "event", "PLAYER_READY",
                            "username", username,
                            "timestamp", System.currentTimeMillis()
                    )
            );

            log.info("Player {} marked ready in room {}", username, roomCode);

        } catch (Exception e) {
            log.error("Error processing ready status: {}", e.getMessage(), e);
            sendErrorToUser(principal, "Failed to update ready status: " + e.getMessage());
        }
    }

    /**
     * ✅ WebSocket endpoint to leave room
     */
    @MessageMapping("/room/leave")
    public void leaveRoom(@Payload Map<String, String> payload, Principal principal) {
        try {
            String username = principal.getName();
            String roomCodeStr = payload.get("roomCode");

            if (roomCodeStr == null) {
                sendErrorToUser(principal, "Room code is required");
                return;
            }

            int roomCode = Integer.parseInt(roomCodeStr);

            // ✅ Security: Validate user is participant
            Room room = roomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new BadRequestException("Room not found"));

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            boolean isParticipant = room.getMadeBy().getId().equals(currentUser.getId()) ||
                    (room.getJoinBy() != null && room.getJoinBy().getId().equals(currentUser.getId()));

            if (!isParticipant) {
                sendErrorToUser(principal, "You are not in this room");
                return;
            }

            // Notify other player
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode + "/status",
                    Map.of(
                            "event", "PLAYER_LEFT",
                            "username", username,
                            "message", username + " has left the room",
                            "timestamp", System.currentTimeMillis()
                    )
            );

            log.info("Player {} left room {}", username, roomCode);

        } catch (Exception e) {
            log.error("Error processing leave room: {}", e.getMessage(), e);
            sendErrorToUser(principal, "Failed to leave room: " + e.getMessage());
        }
    }

    /**
     * ✅ Helper method to send error to specific user
     */
    private void sendErrorToUser(Principal principal, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                Map.of(
                        "error", errorMessage,
                        "timestamp", System.currentTimeMillis()
                )
        );
    }
}