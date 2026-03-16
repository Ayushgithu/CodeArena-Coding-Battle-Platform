package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.ChatMessageDTO;
import com.codearena.backend.entity.ChatMessage;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.ChatMessageRepository;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.ChatService;
import com.codearena.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;


    /**
     * ✅ SECURITY FIXED: Now uses authenticated user from SecurityContext
     * instead of trusting client-provided username
     */
    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO dto, String username) {
        log.info("Saving chat message for room: {}", dto.getRoomCode());

        // ✅ SECURITY: Get authenticated user from Repository using passed username
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Validate room exists
        Room room = roomRepository.findByRoomCode(dto.getRoomCode())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + dto.getRoomCode()));

        // ✅ SECURITY: Verify user is a participant in this room
        boolean isParticipant = room.getMadeBy().getId().equals(sender.getId()) ||
                (room.getJoinBy() != null && room.getJoinBy().getId().equals(sender.getId()));

        if (!isParticipant) {
            throw new BadRequestException("You are not a participant in this room");
        }

        // ✅ Validate message content
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty");
        }

        if (dto.getContent().length() > 1000) {
            throw new BadRequestException("Message content too long (max 1000 characters)");
        }

        // Create and save chat message
        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent().trim());
        message.setRoom(room);
        message.setSender(sender);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Chat message saved successfully by user: {}", sender.getUsername());

        return mapToDTO(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(int roomCode) {
        log.info("Fetching chat history for room: {}", roomCode);

        // ✅ SECURITY: Verify current user is participant
        User currentUser = userService.getCurrentUser();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + roomCode));

        boolean isParticipant = room.getMadeBy().getId().equals(currentUser.getId()) ||
                (room.getJoinBy() != null && room.getJoinBy().getId().equals(currentUser.getId()));

        if (!isParticipant) {
            throw new BadRequestException("You don't have permission to view this chat history");
        }

        return chatMessageRepository.findByRoomRoomCode(roomCode)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(int roomCode) {
        log.info("Fetching messages for room: {}", roomCode);

        // ✅ SECURITY: Verify current user is participant
        User currentUser = userService.getCurrentUser();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with code: " + roomCode));

        boolean isParticipant = room.getMadeBy().getId().equals(currentUser.getId()) ||
                (room.getJoinBy() != null && room.getJoinBy().getId().equals(currentUser.getId()));

        if (!isParticipant) {
            throw new BadRequestException("You don't have permission to view these messages");
        }

        return chatMessageRepository.findByRoom_IdOrderByCreationDateAsc(room.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map ChatMessage entity to DTO
     */
    private ChatMessageDTO mapToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setContent(message.getContent());
        dto.setRoomCode(message.getRoom().getRoomCode());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setTimestamp(message.getCreationDate());
        return dto;
    }
}