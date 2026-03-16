package com.codearena.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInvitationDTO {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @Min(value = 100000, message = "Invalid room code")
    private int roomCode; // Remove @NotBlank for int

    private String customMessage;

    private String inviterName;
}