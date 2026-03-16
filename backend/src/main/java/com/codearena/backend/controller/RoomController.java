package com.codearena.backend.controller;


import com.codearena.backend.dto.QuestionFetchDTO;
import com.codearena.backend.dto.RoomRequestDTO;
import com.codearena.backend.dto.RoomResponseDTO;
import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.service.RoomQuestionService;
import com.codearena.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomQuestionService roomQuestionService;
    // ✅ Create a room
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody RoomRequestDTO roomRequestDTO) {
        System.out.println(roomRequestDTO);
        RoomResponseDTO response = roomService.createRoom(roomRequestDTO);
        roomQuestionService.assignQuestionsToRoom(response.getRoomCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponse.success("Room created successfully", response));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestParam int roomCode, Principal principal) {
        System.out.println(principal.getName());
        System.out.println(principal);
        System.out.println("User attempting to join room: " + roomCode);
        RoomResponseDTO room = roomService.joinRoom(roomCode);

        // Notify client side when a player joins, allowing the creator to trigger 'start match'
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode + "/status",
                Map.of(
                        "event", "PLAYER_JOINED",
                        "username", principal.getName(),
                        "playerCount", room.getJoinedByName()!=null?2:1
                )
        );

        return ResponseEntity.ok(room);
    }


    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoomDetails(@PathVariable int roomCode) {
        RoomResponseDTO response = roomService.getRoomDetails(roomCode);
        return ResponseEntity.ok(StandardResponse.success("Room details fetched", response));
    }

    // ✅ UPDATED: Starts the room, assigns a question, and returns the question to the client
    @PostMapping("/start")
    public ResponseEntity<?> startRoom(@RequestParam int roomCode) {
        roomService.startRoom(roomCode);
        return ResponseEntity.ok(StandardResponse.success("Room started successfully and question assigned.", null));
    }
    @PostMapping("/end-test/{roomCode}")
    public ResponseEntity<?> endTest(@PathVariable int roomCode) {
        roomService.endTest(roomCode);

        return ResponseEntity.ok(StandardResponse.success("Test ended successfully", null));
    }
}