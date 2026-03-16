package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.*;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.*;
import com.codearena.backend.service.CodeExecutionService;
import com.codearena.backend.service.SubmissionService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.service.UserProfileService; // Added import
import com.codearena.backend.utils.constant.SubmissionStatus;
import com.codearena.backend.utils.constant.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CodingQuestionRepository codingQuestionRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqQuestionOptionRepository mcqQuestionOptionRepository;
    private final SubmissionRepository submissionRepository;
    private final MatchResultRepository matchResultRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeExecutionService codeExecutionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomQuestionRepository roomQuestionRepository;
    private final UserService userService;
    private final UserProfileService userProfileService; // ✅ Added injection for score updates

    @Override
    public RoomResultResponseDTO submitRoomAnswers(SubmissionRequestDTO request, Principal principal) {

        Room room = roomRepository.findByRoomCode(request.getRoomCode()).get();

        List<RoomQuestion> assignedQuestions =
                roomQuestionRepository.findByRoomId(room.getId());

        if (assignedQuestions.isEmpty()) {
            throw new ResourceNotFoundException("No questions assigned to this room");
        }

        // ---------- MCQ FLOW ----------
        if (request.getQuestionType().equalsIgnoreCase("MCQ")) {
            return processMcqSubmission(room, assignedQuestions, request);
        }

        throw new RuntimeException("Unsupported question type");
    }

    private RoomResultResponseDTO processMcqSubmission(
            Room room,
            List<RoomQuestion> assignedQuestions,
            SubmissionRequestDTO request) {

        Map<String, String> answerMap =
                request.getMcqAnswers()
                        .stream()
                        .collect(Collectors.toMap(
                                McqAnswerDTO::getQuestionId,
                                McqAnswerDTO::getSelectedOptionId
                        ));

        int correct = 0;

        for (RoomQuestion rq : assignedQuestions) {
            McqQuestion question = rq.getMcqQuestion();
            if (question == null) continue;

            String submittedOptionId = answerMap.get(question.getId());
            if (submittedOptionId == null) continue;

            McqQuestionOption option =
                    mcqQuestionOptionRepository.findById(submittedOptionId).get();

            if (option.isCorrect()) {
                correct++;
            }
        }

        // ---------- RESULT COMPUTATION ----------
        int total = assignedQuestions.size();
        int score = correct;

        long timeTaken = Duration.between(room.getStartedAt(), LocalDateTime.now()).getSeconds();

        // ---------- SAVE TO SUBMISSION TABLE ----------
        User user = userService.getCurrentUser();

        for (RoomQuestion rq : assignedQuestions) {
            McqQuestion question = rq.getMcqQuestion();
            if (question == null) continue;

            String submittedOptionId = answerMap.get(question.getId());
            if (submittedOptionId == null) continue;

            McqQuestionOption option = mcqQuestionOptionRepository.findById(submittedOptionId).get();

            Submission submission = new Submission();
            submission.setUser(user);
            submission.setRoom(room);
            submission.setMcqQuestion(question);
            submission.setMcqOptionId(submittedOptionId);
            submission.setIsCorrect(option.isCorrect());
            submission.setScore(option.isCorrect() ? 1 : 0);
            submission.setStatus(option.isCorrect() ? SubmissionStatus.ACCEPTED : SubmissionStatus.WRONG_ANSWER);
            submission.setExecutionTime((double)timeTaken);
            submissionRepository.save(submission);
        }

        // ---------- SAVE TO MATCH RESULT ----------
        MatchResult matchResult = matchResultRepository.findByRoomIdAndUserId(room.getId(), user.getId())
                .orElseGet(() -> {
                    MatchResult nmr = new MatchResult();
                    nmr.setRoom(room);
                    nmr.setUser(user);
                    return nmr;
                });

        matchResult.setScore(score);
        matchResult.setTotalTime(timeTaken);
        matchResult.setFinished(true);
        matchResult.setFinishedAt(LocalDateTime.now());
        matchResultRepository.save(matchResult);

        // ✅ CRITICAL CHANGE: Trigger UserProfile Update
        // This adds the points from this match to the user's global ranking score.
//        userProfileService.updateUserStats(user, false, score);

        return new RoomResultResponseDTO(
                String.valueOf(room.getRoomCode()),
                total,
                correct,
                score,
                timeTaken,
                "PENDING"
        );
    }

    @Override
    public List<SubmissionResponseDTO> getAllSubmissionByRoomCodeForCurrentUser(int roomCode) {
        User currentUser = userService.getCurrentUser();
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        List<Submission> submissions =
                submissionRepository.findByRoomIdAndUserId(room.getId(), currentUser.getId());

        return submissions.stream().map(submission -> {
            SubmissionResponseDTO dto = new SubmissionResponseDTO();
            dto.setUserId(submission.getUser().getId());
            dto.setStatus(submission.getStatus().name());
            dto.setScore(submission.getScore());
            dto.setExecutionTime(submission.getExecutionTime());
            dto.setMessage("Submission fetched successfully");
            dto.setSubmissionId(submission.getId());
            if (submission.getMcqQuestion() != null) {
                dto.setQuestionId(submission.getMcqQuestion().getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private User evaluateWinner(Room room, List<Submission> submissions) {
        Submission sub1 = submissions.get(0);
        Submission sub2 = submissions.get(1);

        User winner = null;
        boolean isTie = false;

        if (sub1.getScore() > sub2.getScore()) {
            winner = sub1.getUser();
        } else if (sub2.getScore() > sub1.getScore()) {
            winner = sub2.getUser();
        } else {
            if (sub1.getExecutionTime() < sub2.getExecutionTime()) {
                winner = sub1.getUser();
            } else if (sub2.getExecutionTime() < sub1.getExecutionTime()) {
                winner = sub2.getUser();
            } else {
                isTie = true;
            }
        }

        saveMatchResult(room, sub1, winner, isTie);
        saveMatchResult(room, sub2, winner, isTie);

        room.setStatus(Status.COMPLETED);
        room.setEndedAt(LocalDateTime.now());
        roomRepository.save(room);

        sendMatchCompletedNotification(room, sub1, sub2, winner, isTie);
        return winner;
    }

    private void saveMatchResult(Room room, Submission submission, User winner, boolean isTie) {
        MatchResult matchResult = matchResultRepository.findByRoomIdAndUserId(room.getId(), submission.getUser().getId())
                .orElse(new MatchResult());
        matchResult.setRoom(room);
        matchResult.setUser(submission.getUser());
        matchResult.setScore(submission.getScore());
        matchResult.setTotalTime((long) submission.getExecutionTime());
        matchResult.setWinner(!isTie && winner != null && winner.equals(submission.getUser()));
        matchResultRepository.save(matchResult);
    }

    private void sendMatchCompletedNotification(Room room, Submission sub1, Submission sub2, User winner, boolean isTie) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "MATCH_COMPLETED");
        notification.put("winner", winner != null ? winner.getUsername() : "TIE");
        notification.put("message", isTie ? "It's a Tie!" : winner.getUsername() + " wins!");
        notification.put("player1", sub1.getUser().getUsername());
        notification.put("player1Score", sub1.getScore());
        notification.put("player1Time", sub1.getExecutionTime());
        notification.put("player2", sub2.getUser().getUsername());
        notification.put("player2Score", sub2.getScore());
        notification.put("player2Time", sub2.getExecutionTime());
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomCode() + "/status", notification);
    }

    // Helper classes and methods remain unchanged below
    public String getLanguageVersion(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "3.10.0";
            case "java" -> "15.0.2";
            case "cpp", "c++" -> "10.2.0";
            case "c" -> "10.2.0";
            case "javascript" -> "18.15.0";
            default -> "latest";
        };
    }

    private static class CodingExecutionResult {
        int passedCount;
        double totalExecutionTime;
        double maxMemoryUsed;
        boolean compilationFailed;
        String compilationError;

        CodingExecutionResult(int passedCount, double totalExecutionTime, double maxMemoryUsed,
                              boolean compilationFailed, String compilationError) {
            this.passedCount = passedCount;
            this.totalExecutionTime = totalExecutionTime;
            this.maxMemoryUsed = maxMemoryUsed;
            this.compilationFailed = compilationFailed;
            this.compilationError = compilationError;
        }
    }
}