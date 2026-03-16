package com.codearena.backend;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.service.EmailService;
import com.codearena.backend.service.EmailTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailConnectionTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Test
    public void testSendEmail() {
        try {
            System.out.println("Attempting to send test email...");
            
            // Test Template Generation (this was causing the error)
            System.out.println("Testing Template Generation...");
            Room room = new Room();
            room.setRoomCode(123456);
            room.setNoOfQuestion(5);
            
            RoomInvitationDTO invitationDTO = new RoomInvitationDTO();
            invitationDTO.setRecipientEmail("itzzadi45@gmail.com");
            invitationDTO.setCustomMessage("Test Message");
            
            String htmlBody = emailTemplateService.createRoomInvitationHtml(room, null, invitationDTO, "http://localhost:5173/join");
            System.out.println("Template generated successfully!");
            
            // Test HTML Mail with Template
            System.out.println("Attempting to send generated HTML email...");
            emailService.sendHtmlMail("itzzadi45@gmail.com", "Test Template Email", htmlBody);
            
            System.out.println("Test emails sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send test email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
