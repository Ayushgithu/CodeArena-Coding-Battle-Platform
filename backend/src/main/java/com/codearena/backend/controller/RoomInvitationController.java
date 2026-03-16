package com.codearena.backend.controller;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.exception.EmailSendingException;
import com.codearena.backend.service.RoomInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/room-invitations")
@RequiredArgsConstructor
public class RoomInvitationController {

    private static final Logger logger = LoggerFactory.getLogger(RoomInvitationController.class);

    private final RoomInvitationService roomInvitationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendRoomInvitation(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RoomInvitationDTO invitationDTO) {

        try {
            roomInvitationService.sendRoomInvitation(invitationDTO);

            return ResponseEntity.ok(
                    StandardResponse.success(
                            "Room invitation sent successfully to " + invitationDTO.getRecipientEmail(),
                            null
                    )
            );

        } catch (EmailSendingException e) {
            logger.error("Failed to send room invitation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to send invitation: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Error sending room invitation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Error sending invitation: " + e.getMessage()));
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<?> sendBulkInvitations(
            @RequestHeader("Authorization") String token,
            @RequestParam int roomCode,
            @RequestParam String emails, // Comma-separated emails
            @RequestParam(required = false) String inviterName) {

        try {
            // Split comma-separated emails
            String[] emailArray = emails.split(",");
            // Trim whitespace from each email
            emailArray = Arrays.stream(emailArray)
                    .map(String::trim)
                    .filter(email -> !email.isEmpty())
                    .toArray(String[]::new);

            if (emailArray.length == 0) {
                return ResponseEntity.badRequest()
                        .body(StandardResponse.error("No valid email addresses provided"));
            }

            roomInvitationService.sendBulkRoomInvitations(roomCode, emailArray, inviterName);

            return ResponseEntity.ok(
                    StandardResponse.success(
                            String.format("Invitations sent successfully to %d recipients", emailArray.length),
                            null
                    )
            );

        } catch (EmailSendingException e) {
            logger.error("Failed to send bulk invitations: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to send some invitations: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Error sending bulk invitations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Error sending invitations: " + e.getMessage()));
        }
    }

    @PostMapping("/send-to-friends/{roomCode}")
    public ResponseEntity<?> sendInvitationToFriends(
            @RequestHeader("Authorization") String token,
            @PathVariable int roomCode) {

        try {
            roomInvitationService.sendInvitationToFriends(roomCode);

            return ResponseEntity.ok(
                    StandardResponse.success(
                            "Invitations sent to friends successfully",
                            null
                    )
            );

        } catch (Exception e) {
            logger.error("Error sending invitations to friends: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Error: " + e.getMessage()));
        }
    }
}