package com.codearena.backend.dto;


import lombok.Data;
import java.util.Date;

@Data
public class ChatMessageDTO {

    // The message content
    private String content;

    // The 'roomCode' (e.g., 123456) to identify the room
    private int roomCode;

    // The 'username' (email) of the sender.
    // !! FOR PHASE 1, we will trust the client to send this.
    // !! This is INSECURE and we will fix it in Phase 2.
    private String senderUsername;

    // The timestamp when the message was saved, sent back from the server
    private Date timestamp;

    // like for typing..............
    private String type;
}
