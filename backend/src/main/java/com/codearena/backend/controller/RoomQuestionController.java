package com.codearena.backend.controller;

import com.codearena.backend.service.RoomQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomQuestionController {

    private final RoomQuestionService roomQuestionService;

    // Assign questions when match starts
    @PostMapping("/{roomCode}/assign-questions")
    public ResponseEntity<String> assignQuestions(@PathVariable int roomCode) {

        roomQuestionService.assignQuestionsToRoom(roomCode);

        return ResponseEntity.ok("Questions assigned successfully");
    }

    // Fetch assigned questions
    @GetMapping("/{roomCode}/questions")
    public ResponseEntity<?> getRoomQuestions(
            @PathVariable int roomCode) {
        return ResponseEntity.ok(
                roomQuestionService.getRoomQuestions(roomCode)
        );
    }

    @GetMapping("/{roomCode}/coding-questions")
    public ResponseEntity<?> getRoomCodingQuestions(
            @PathVariable int roomCode) {

        return ResponseEntity.ok(
                roomQuestionService.getRoomCodingQuestions(roomCode)
        );
    }

    @GetMapping("/{roomCode}/question-status")
    public ResponseEntity<?> getQuestionStatus(
            @PathVariable int roomCode) {

        return ResponseEntity.ok(
                roomQuestionService.getRoomQuestionStatus(roomCode)
        );
    }

}

