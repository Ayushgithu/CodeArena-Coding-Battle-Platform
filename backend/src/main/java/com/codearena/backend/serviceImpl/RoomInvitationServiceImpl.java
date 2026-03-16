package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.EmailSendingException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.service.EmailService;
import com.codearena.backend.service.EmailTemplateService;
import com.codearena.backend.service.RoomInvitationService;
import com.codearena.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RoomInvitationServiceImpl implements RoomInvitationService {

    private static final Logger logger = LoggerFactory.getLogger(RoomInvitationServiceImpl.class);

    private final RoomRepository roomRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.invitation.expiry-hours:24}")
    private int invitationExpiryHours;

    @Value("${app.invitation.use-html-emails:true}")
    private boolean useHtmlEmails;

    @Override
    public void sendRoomInvitation(RoomInvitationDTO invitationDTO) throws EmailSendingException {
        // Validate room exists
        Room room = roomRepository.findByRoomCode(invitationDTO.getRoomCode())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + invitationDTO.getRoomCode()));

        // Check if room is already full
        if (room.getJoinBy() != null) {
            throw new BadRequestException("Room is already full");
        }

        // Get current user as inviter
        User inviter = userService.getCurrentUser();

        // Verify inviter is the room creator
        if (!room.getMadeBy().getId().equals(inviter.getId())) {
            throw new BadRequestException("Only room creator can send invitations");
        }

        // Build invitation link
        String invitationLink = buildInvitationLink(invitationDTO.getRoomCode());

        // Prepare email content
        String subject = buildEmailSubject(room, inviter, invitationDTO);

        if (useHtmlEmails) {
            // Send HTML email
            String htmlBody = emailTemplateService.createRoomInvitationHtml(room, inviter, invitationDTO, invitationLink);
            emailService.sendHtmlMail(invitationDTO.getRecipientEmail(), subject, htmlBody);
        } else {
            // Send plain text email
            String plainBody = buildEmailBody(room, inviter, invitationDTO, invitationLink);
            emailService.sendSimpleMail(invitationDTO.getRecipientEmail(), subject, plainBody);
        }

        logger.info("Room invitation sent successfully. Room: {}, Recipient: {}, Inviter: {}",
                invitationDTO.getRoomCode(), invitationDTO.getRecipientEmail(), inviter.getUsername());
    }

    @Override
    public void sendBulkRoomInvitations(int roomCode, String[] recipientEmails, String inviterName) throws EmailSendingException {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + roomCode));

        // Check if room is already full
        if (room.getJoinBy() != null) {
            throw new BadRequestException("Room is already full");
        }

        User inviter = userService.getCurrentUser();

        // Verify inviter is the room creator
        if (!room.getMadeBy().getId().equals(inviter.getId())) {
            throw new BadRequestException("Only room creator can send invitations");
        }

        String invitationLink = buildInvitationLink(roomCode);
        int successCount = 0;
        int failureCount = 0;

        for (String recipientEmail : recipientEmails) {
            try {
                RoomInvitationDTO invitationDTO = RoomInvitationDTO.builder()
                        .recipientEmail(recipientEmail)
                        .roomCode(roomCode)
                        .inviterName(inviterName != null ? inviterName : inviter.getUsername())
                        .customMessage("Join me for a coding challenge!")
                        .build();

                String subject = buildEmailSubject(room, inviter, invitationDTO);

                if (useHtmlEmails) {
                    String htmlBody = emailTemplateService.createRoomInvitationHtml(room, inviter, invitationDTO, invitationLink);
                    emailService.sendHtmlMail(recipientEmail, subject, htmlBody);
                } else {
                    String plainBody = buildEmailBody(room, inviter, invitationDTO, invitationLink);
                    emailService.sendSimpleMail(recipientEmail, subject, plainBody);
                }

                successCount++;
                logger.info("Bulk invitation sent to: {} for room: {}", recipientEmail, roomCode);

            } catch (EmailSendingException e) {
                failureCount++;
                logger.error("Failed to send invitation to {}: {}", recipientEmail, e.getMessage());
                // Continue with other emails even if one fails
            }
        }

        logger.info("Bulk invitation completed. Success: {}, Failed: {}", successCount, failureCount);
    }

    @Override
    public void sendInvitationToFriends(int roomCode) throws EmailSendingException {
        // This is a placeholder - you would integrate with your friend system
        // For now, we'll throw an exception indicating this feature needs implementation

        logger.warn("sendInvitationToFriends not implemented yet. Room code: {}", roomCode);
        throw new BadRequestException("Friend invitation feature is not yet implemented");
    }

    private String buildInvitationLink(int roomCode) {
        return String.format("%s/join-room?code=%d", frontendUrl, roomCode);
    }

    private String buildEmailSubject(Room room, User inviter, RoomInvitationDTO invitationDTO) {
        String inviterName = invitationDTO.getInviterName() != null ?
                invitationDTO.getInviterName() : inviter.getUsername();

        return String.format("Join %s's Coding Challenge on CodeArena!", inviterName);
    }

    private String buildEmailBody(Room room, User inviter, RoomInvitationDTO invitationDTO, String invitationLink) {
        String inviterName = invitationDTO.getInviterName() != null ?
                invitationDTO.getInviterName() : inviter.getUsername();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        String expiryTime = room.getExpiryTime() != null ?
                room.getExpiryTime().format(formatter) : "Not specified";

        StringBuilder body = new StringBuilder();

        body.append("Hello!\n\n");

        if (invitationDTO.getCustomMessage() != null && !invitationDTO.getCustomMessage().isBlank()) {
            body.append(inviterName).append(" says: ").append(invitationDTO.getCustomMessage()).append("\n\n");
        }

        body.append("You've been invited to join a coding challenge on CodeArena!\n\n");
        body.append("Challenge Details:\n");
        body.append("------------------\n");
        body.append("Room Code: ").append(room.getRoomCode()).append("\n");
        body.append("Invited by: ").append(inviterName).append("\n");
        body.append("Question Type: ").append(room.getQuestionType() != null ?
                room.getQuestionType().getOptionValue() : "Mixed").append("\n");
        body.append("Difficulty: ").append(room.getDifficulty() != null ?
                room.getDifficulty().name() : "Mixed").append("\n");
        body.append("Number of Questions: ").append(room.getNoOfQuestion()).append("\n");
        body.append("Room Expires: ").append(expiryTime).append("\n\n");

        body.append("Join the challenge here:\n");
        body.append(invitationLink).append("\n\n");

        body.append("Or manually join using room code: ").append(room.getRoomCode()).append("\n\n");

        body.append("Instructions:\n");
        body.append("1. Click the link above or go to CodeArena\n");
        body.append("2. Click 'Join Room'\n");
        body.append("3. Enter the room code: ").append(room.getRoomCode()).append("\n");
        body.append("4. Start coding!\n\n");

        body.append("This invitation will expire in ").append(invitationExpiryHours).append(" hours.\n\n");

        body.append("Best of luck!\n");
        body.append("The CodeArena Team\n\n");
        body.append("--\n");
        body.append("This is an automated message. Please do not reply to this email.\n");

        return body.toString();
    }
}