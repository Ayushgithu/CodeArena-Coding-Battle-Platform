package com.codearena.backend.service;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeQuestionService {

    /**
     * Start a new practice session
     */
    PracticeSessionDTO startPracticeSession(PracticeMatchRequestDTO request, User user);

    /**
     * Get next question for the practice session with adaptive difficulty
     */
    PracticeQuestionResponseDTO getNextQuestion(String sessionId, User user);

    /**
     * Submit answer for current question and get next question
     */
//    need to evaluate after it
//    PracticeQuestionResponseDTO submitAndGetNext(PracticeSubmissionDTO submission, User user);

    /**
     * Submit current question and get result (without fetching next question)
     */

//    need to evaluate after it
    SubmissionResultDTO submitCurrentQuestion(PracticeSubmissionDTO submission, User user);

    /**
     * End practice session and get comprehensive results
     */
    PracticeResultDTO endPracticeSession(String sessionId, User user);

    /**
     * Resume an existing practice session
     */
    PracticeSessionDTO resumePracticeSession(String sessionId, User user);

    /**
     * Get session details
     */
    PracticeSessionDTO getSessionDetails(String sessionId, User user);

    /**
     * Get all active practice sessions for user
     */
    List<PracticeSessionDTO> getActiveSessions(User user);

    /**
     * Get practice history for a user within a date range
     */
    List<PracticeHistoryDTO> getPracticeHistory(User user, LocalDateTime startDate, LocalDateTime endDate);
    void extendSessionTime(String sessionId, int additionalMinutes, User user);
    void skipQuestion(String sessionId, User user);

    void abandonSession(String sessionId, User user);
}