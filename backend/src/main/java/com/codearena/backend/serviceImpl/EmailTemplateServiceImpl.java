package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.service.EmailTemplateService;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    public String createRoomInvitationHtml(Room room, User inviter,
                                           RoomInvitationDTO invitationDTO, String invitationLink) {

        String inviterName = invitationDTO.getInviterName() != null
                ? invitationDTO.getInviterName()
                : (inviter != null ? inviter.getUsername() : "Unknown User");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>CodeArena Room Invitation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                    .room-details { background: white; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #667eea; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏆 CodeArena Challenge Invitation</h1>
                    <p>You've been invited to a coding battle!</p>
                </div>
                
                <div class="content">
                    <h2>Hello!</h2>
                    
                    <p><strong>%s</strong> has invited you to join a coding challenge on CodeArena.</p>
                    
                    %s
                    
                    <div class="room-details">
                        <h3>📋 Challenge Details</h3>
                        <p><strong>Room Code:</strong> <span style="background: #eef; padding: 5px 10px; border-radius: 3px; font-family: monospace;">%d</span></p>
                        <p><strong>Question Type:</strong> %s</p>
                        <p><strong>Difficulty:</strong> %s</p>
                        <p><strong>Number of Questions:</strong> %d</p>
                        <p><strong>Invited by:</strong> %s</p>
                    </div>
                    
                    <div style="text-align: center;">
                        <a href="%s" class="button">🎯 Join Challenge Now</a>
                    </div>
                    
                    <p>Or copy this link: <br><code style="word-break: break-all; color: #667eea;">%s</code></p>
                    
                    <h3>📝 How to Join:</h3>
                    <ol>
                        <li>Click the button above or paste the link in your browser</li>
                        <li>If you don't have an account, you'll be prompted to sign up (it's free!)</li>
                        <li>Enter the room code: <strong>%d</strong></li>
                        <li>Start coding and compete!</li>
                    </ol>
                    
                    <p><strong>⏰ Important:</strong> This invitation is valid for 24 hours.</p>
                    
                    <p>Good luck and happy coding! 🚀</p>
                    
                    <p>Best regards,<br>The CodeArena Team</p>
                </div>
                
                <div class="footer">
                    <p>This is an automated message from CodeArena. Please do not reply to this email.</p>
                    <p>If you didn't expect this invitation, you can safely ignore it.</p>
                    <p>© 2024 CodeArena. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(
                inviterName,
                invitationDTO.getCustomMessage() != null ?
                        "<p><em>\"" + invitationDTO.getCustomMessage() + "\"</em></p>" : "",
                room.getRoomCode(),
                room.getQuestionType() != null ? room.getQuestionType().getOptionValue() : "Mixed",
                room.getDifficulty() != null ? room.getDifficulty().name() : "Mixed",
                room.getNoOfQuestion(),
                inviterName,
                invitationLink,
                invitationLink,
                room.getRoomCode()
        );
    }
}

