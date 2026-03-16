package com.codearena.backend.controller;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.User;
import com.codearena.backend.service.PracticeQuestionService;
import com.codearena.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/practice")
@RequiredArgsConstructor
@Slf4j
public class PracticeController {

    private final PracticeQuestionService practiceQuestionService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<StandardResponse<PracticeSessionDTO>> startPracticeSession(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PracticeMatchRequestDTO request) {

        try {
            User currentUser = userService.getCurrentUser();
            PracticeSessionDTO session = practiceQuestionService.startPracticeSession(request, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Practice session started successfully", session)
            );

        } catch (Exception e) {
            log.error("Error starting practice session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to start practice session: " + e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}/next")
    public ResponseEntity<StandardResponse<PracticeQuestionResponseDTO>> getNextQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {

        try {
            User currentUser = userService.getCurrentUser();
            PracticeQuestionResponseDTO nextQuestion = practiceQuestionService.getNextQuestion(sessionId, currentUser);

            if (nextQuestion == null) {
                return ResponseEntity.ok(
                        StandardResponse.success("Session completed", null)
                );
            }

            return ResponseEntity.ok(
                    StandardResponse.success("Next question fetched successfully", nextQuestion)
            );

        } catch (Exception e) {
            log.error("Error getting next question: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to get next question: " + e.getMessage()));
        }
    }

//    @PostMapping("/submit")
//    public ResponseEntity<StandardResponse<PracticeQuestionResponseDTO>> submitAnswer(
//            @RequestHeader("Authorization") String token,
//            @Valid @RequestBody PracticeSubmissionDTO submission) {
//
//        try {
//            User currentUser = userService.getCurrentUser();
//
//            // Validate submission based on question type
//            if ("CODING".equals(submission.getQuestionType())) {
//                if (submission.getLanguage() == null || submission.getSourceCode() == null) {
//                    return ResponseEntity.badRequest()
//                            .body(StandardResponse.error("For coding questions, language and source code are required"));
//                }
//            } else if ("MCQ".equals(submission.getQuestionType())) {
//                if (submission.getSelectedOptionId() == null) {
//                    return ResponseEntity.badRequest()
//                            .body(StandardResponse.error("For MCQ questions, selected option is required"));
//                }
//            } else {
//                return ResponseEntity.badRequest()
//                        .body(StandardResponse.error("Invalid question type"));
//            }
//
//            PracticeQuestionResponseDTO nextQuestion = practiceQuestionService.submitAndGetNext(submission, currentUser);
//
//            if (nextQuestion == null) {
//                return ResponseEntity.ok(
//                        StandardResponse.success("Session completed. No more questions.", null)
//                );
//            }
//
//            return ResponseEntity.ok(
//                    StandardResponse.success("Answer submitted successfully", nextQuestion)
//            );
//
//        } catch (Exception e) {
//            log.error("Error submitting answer: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest()
//                    .body(StandardResponse.error("Failed to submit answer: " + e.getMessage()));
//        }
//    }

    // ✅ NEW: Submit current question without getting next
    @PostMapping("/submit-current")
    public ResponseEntity<StandardResponse<SubmissionResultDTO>> submitCurrentQuestion(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PracticeSubmissionDTO submission) {

        try {
            User currentUser = userService.getCurrentUser();
            SubmissionResultDTO result = practiceQuestionService.submitCurrentQuestion(submission, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Answer submitted successfully", result)
            );

        } catch (Exception e) {
            log.error("Error submitting current question: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to submit answer: " + e.getMessage()));
        }
    }

    @PostMapping("/session/{sessionId}/end")
    public ResponseEntity<StandardResponse<PracticeResultDTO>> endPracticeSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {

        try {
            User currentUser = userService.getCurrentUser();
            PracticeResultDTO result = practiceQuestionService.endPracticeSession(sessionId, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Practice session completed successfully", result)
            );

        } catch (Exception e) {
            log.error("Error ending practice session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to end practice session: " + e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}/resume")
    public ResponseEntity<StandardResponse<PracticeSessionDTO>> resumePracticeSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {

        try {
            User currentUser = userService.getCurrentUser();
            PracticeSessionDTO session = practiceQuestionService.resumePracticeSession(sessionId, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Practice session resumed successfully", session)
            );

        } catch (Exception e) {
            log.error("Error resuming practice session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to resume practice session: " + e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<StandardResponse<PracticeSessionDTO>> getSessionDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {
        try {
            User currentUser = userService.getCurrentUser();
            PracticeSessionDTO session = practiceQuestionService.getSessionDetails(sessionId, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Session details fetched successfully", session)
            );

        } catch (Exception e) {
            log.error("Error getting session details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to get session details: " + e.getMessage()));
        }
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<StandardResponse<List<PracticeSessionDTO>>> getActiveSessions(
            @RequestHeader("Authorization") String token) {

        try {
            User currentUser = userService.getCurrentUser();
            List<PracticeSessionDTO> sessions = practiceQuestionService.getActiveSessions(currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Active sessions fetched successfully", sessions)
            );

        } catch (Exception e) {
            log.error("Error getting active sessions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to get active sessions: " + e.getMessage()));
        }
    }

    // ✅ NEW: Get practice history
    @GetMapping("/history")
    public ResponseEntity<StandardResponse<List<PracticeHistoryDTO>>> getPracticeHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            User currentUser = userService.getCurrentUser();

            // Default to last 30 days if no dates provided
            LocalDateTime start = startDate != null ?
                    LocalDateTime.parse(startDate) :
                    LocalDateTime.now().minusDays(30);

            LocalDateTime end = endDate != null ?
                    LocalDateTime.parse(endDate) :
                    LocalDateTime.now();

            List<PracticeHistoryDTO> history = practiceQuestionService.getPracticeHistory(
                    currentUser, start, end
            );

            return ResponseEntity.ok(
                    StandardResponse.success("Practice history fetched successfully", history)
            );

        } catch (Exception e) {
            log.error("Error getting practice history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to get practice history: " + e.getMessage()));
        }
    }

    // ✅ NEW: Extend session time
    @PostMapping("/session/{sessionId}/extend")
    public ResponseEntity<StandardResponse<Void>> extendSessionTime(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId,
            @RequestParam int additionalMinutes) {

        try {
            if (additionalMinutes <= 0 || additionalMinutes > 60) {
                return ResponseEntity.badRequest()
                        .body(StandardResponse.error("Additional minutes must be between 1 and 60"));
            }

            User currentUser = userService.getCurrentUser();
            practiceQuestionService.extendSessionTime(sessionId, additionalMinutes, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success(
                            "Session extended by " + additionalMinutes + " minutes",
                            null
                    )
            );

        } catch (Exception e) {
            log.error("Error extending session time: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to extend session: " + e.getMessage()));
        }
    }

    // ✅ NEW: Skip current question
    @PostMapping("/session/{sessionId}/skip")
    public ResponseEntity<StandardResponse<PracticeQuestionResponseDTO>> skipQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {

        try {
            User currentUser = userService.getCurrentUser();
            practiceQuestionService.skipQuestion(sessionId, currentUser);

            // Get next question after skip
            PracticeQuestionResponseDTO nextQuestion =
                    practiceQuestionService.getNextQuestion(sessionId, currentUser);

            if (nextQuestion == null) {
                return ResponseEntity.ok(
                        StandardResponse.success("Question skipped. Session completed.", null)
                );
            }

            return ResponseEntity.ok(
                    StandardResponse.success("Question skipped successfully", nextQuestion)
            );

        } catch (Exception e) {
            log.error("Error skipping question: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to skip question: " + e.getMessage()));
        }
    }

    // ✅ NEW: Abandon session
    @PostMapping("/session/{sessionId}/abandon")
    public ResponseEntity<StandardResponse<Void>> abandonSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {

        try {
            User currentUser = userService.getCurrentUser();
            practiceQuestionService.abandonSession(sessionId, currentUser);

            return ResponseEntity.ok(
                    StandardResponse.success("Practice session abandoned", null)
            );

        } catch (Exception e) {
            log.error("Error abandoning session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Failed to abandon session: " + e.getMessage()));
        }
    }
}