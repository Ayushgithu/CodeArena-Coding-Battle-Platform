package com.codearena.backend.service;


import com.codearena.backend.dto.ChatMessageDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ChatService {

    /**
     * Saves a new chat message and returns the saved message as a DTO.
     */
    ChatMessageDTO saveMessage(ChatMessageDTO chatMessageDTO, String username);

    /**
     * Gets the full chat history for a specific room.
     */
    List<ChatMessageDTO> getChatHistory(int roomId);

    List<ChatMessageDTO> getMessages(int roomId);
}
