package com.codearena.backend.controller;

import com.codearena.backend.dto.*;
import com.codearena.backend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // This is the unified submission endpoint (Phase 4)
//    @PostMapping
//    public ResponseEntity<?> submitAnswer(
//            @RequestHeader("Authorization") String token, // Token is required for security/auth
//            @RequestBody SubmissionRequestDTO request,
//            Principal principal) {
//
//        SubmissionResponseDTO response = submissionService.processSubmission(request, principal);
//
//        // This response is for the immediate result of the user's submission,
//        // not the final match result (which is handled via WebSocket in the service).
//        return ResponseEntity.ok(
//                StandardResponse.success("Submission processed successfully. Waiting for match results.", response)
//        );
//    }
    @PostMapping("/submit")
    public ResponseEntity<RoomResultResponseDTO> submitRoomAnswers(
            @RequestBody SubmissionRequestDTO submissionRequestDTO,Principal principal) {

        RoomResultResponseDTO result =
                submissionService.submitRoomAnswers(submissionRequestDTO,principal);

        return ResponseEntity.ok(result);
    }
    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getAllSubmissionByRoomCodeForCurrentUser(@RequestHeader("Authorization") String token,
                                                                      @PathVariable int roomCode){
        return ResponseEntity.ok(
                StandardResponse.success("Submissions fetched successfully",
                        submissionService.getAllSubmissionByRoomCodeForCurrentUser(roomCode))
        );
    }

}