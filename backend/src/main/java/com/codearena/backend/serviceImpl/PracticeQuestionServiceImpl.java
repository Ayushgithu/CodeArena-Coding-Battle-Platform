package com.codearena.backend.serviceImpl;

import com.codearena.backend.config.AppProperties;
import com.codearena.backend.dto.*;
import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.PracticeSessionRepository;
import com.codearena.backend.service.*;
import com.codearena.backend.service.ai.AdaptiveQuestionSelector;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.ErrorMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ✅ IMPROVED: Complete practice question service with all methods
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeQuestionServiceImpl implements PracticeQuestionService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final AdaptiveQuestionSelector adaptiveQuestionSelector;
    private final QuestionFetcherService questionFetcherService;
    private final McqEvaluationService mcqEvaluationService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final ValidationService validationService;
    private final NotificationService notificationService;
    private final CodingQuestionRepository codingQuestionRepository;
    private final CodeExecutionService codeExecutionService;
    private final SubmissionServiceImpl submissionService;
    @Override
    @Transactional
    public PracticeSessionDTO startPracticeSession(PracticeMatchRequestDTO request, User user) {
        log.info("Starting practice session for user: {}", user.getUsername());

        // ✅ Use centralized validation
        validationService.validatePracticeRequest(request);

        // Determine starting difficulty
        Difficulty startingDifficulty = determineStartingDifficulty(request, user);

        // Create practice session
        PracticeSession session = new PracticeSession();
        session.setUser(user);
        session.setSessionCode(generateSessionCode());
        session.setStartingDifficulty(startingDifficulty);
        session.setCurrentDifficulty(startingDifficulty);
        session.setQuestionType(request.getQuestionType().toUpperCase());
        session.setMaxQuestions(request.getMaxQuestions());
        session.setTimeLimitMinutes(request.getTimeLimitMinutes());
        session.setTopic(request.getTopic());
        session.setStartedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(request.getTimeLimitMinutes()));
        session.setCurrentQuestionIndex(0);
        session.setTotalQuestionsAnswered(0);
        session.setCorrectAnswers(0);
        session.setAnsweredQuestionIds("[]");
        session.setPerformanceHistory("[]");
        session.setPerformanceMetrics(new HashMap<>());
        session.setAverageTimePerQuestion(0.0);
        session.setAccuracyPercentage(0.0);
        session.setCompleted(false);
        session.setExpired(false);

        PracticeSession savedSession = practiceSessionRepository.save(session);
        log.info("Practice session started: {} for user: {}", savedSession.getId(), user.getUsername());

        // Send notification
        notificationService.sendPracticeSessionStarted(user, savedSession.getId());

        return mapToSessionDTO(savedSession);
    }

    @Override
    @Transactional
    public PracticeQuestionResponseDTO getNextQuestion(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);
        validateSessionActive(session);

        // If first question hasn't been set yet
        if (session.getCurrentQuestionId() == null || session.getCurrentQuestionId().isEmpty()) {
            return getFirstQuestion(session);
        }

        // Check if the current question is still pending (not answered yet)
        Set<String> answeredIds = getUsedQuestionIds(session);
        if (!answeredIds.contains(session.getCurrentQuestionId())) {
            return questionFetcherService.fetchQuestion(session.getCurrentQuestionId(), session.getQuestionType(), session);
        }

        // Check if session is complete based on unique questions answered
        if (answeredIds.size() >= session.getMaxQuestions()) {
            completeSession(session);
            return null;
        }

        return getNextQuestionForSession(session);
    }

//    @Override
//    @Transactional
//    public PracticeQuestionResponseDTO submitAndGetNext(PracticeSubmissionDTO submission, User user) {
//        PracticeSession session = getSession(submission.getSessionId(), user);
//        validateSessionActive(session);
//
//        // Process current submission
//        processSubmission(submission, session);
//
//        // Check if session is complete
//        if (session.getCurrentQuestionIndex() >= session.getMaxQuestions() - 1) {
//            completeSession(session);
//            return null;
//        }
//
//        // Get next question
//        return getNextQuestionForSession(session);
//    }

    @Override
    @Transactional
    public SubmissionResultDTO submitCurrentQuestion(PracticeSubmissionDTO submission, User user) {
        PracticeSession session = getSession(submission.getSessionId(), user);
        validateSessionActive(session);

        // Process submission and get result
        boolean isCorrect = processSubmission(submission, session);

        // Build result DTO
        return buildSubmissionResult(submission, session, isCorrect);
    }

    @Override
    @Transactional
    public PracticeResultDTO endPracticeSession(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);

        if (!session.isCompleted()) {
            completeSession(session);
        }

        // Generate AI recommendations
        RecommendationsDTO recommendations = adaptiveQuestionSelector.generateRecommendations(session);

        // Build result
        PracticeResultDTO result = PracticeResultDTO.builder()
                .sessionId(session.getId())
                .userId(user.getId())
                .userName(user.getUsername())
                .sessionCode(session.getSessionCode())
                .questionType(session.getQuestionType())
                .totalQuestions(session.getTotalQuestionsAnswered())
                .correctAnswers(session.getCorrectAnswers())
                .incorrectAnswers(session.getTotalQuestionsAnswered() - session.getCorrectAnswers())
                .accuracyPercentage(session.getAccuracyPercentage())
                .totalTimeTakenSeconds(calculateTotalTime(session))
                .averageTimePerQuestion(session.getAverageTimePerQuestion())
                .overallDifficultyLevel(calculateOverallDifficulty(session))
                .aiFeedback(generateAIFeedback(session))
                .recommendations(recommendations)
                .performanceMetrics(session.getPerformanceMetrics())
                .startedAt(session.getStartedAt())
                .completedAt(session.getExpiresAt())
                .build();

        // Send notification
        notificationService.sendPracticeSessionCompleted(user, session.getId(), result);

        return result;
    }

    @Override
    @Transactional
    public PracticeSessionDTO resumePracticeSession(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);

        if (session.isCompleted()) {
            throw new BadRequestException(ErrorMessages.SESSION_ALREADY_COMPLETED);
        }

        if (session.isExpired()) {
            session.setExpired(true);
            practiceSessionRepository.save(session);
            throw new BadRequestException(ErrorMessages.SESSION_EXPIRED);
        }

        return mapToSessionDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeSessionDTO getSessionDetails(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);
        return mapToSessionDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeSessionDTO> getActiveSessions(User user) {
        List<PracticeSession> sessions = practiceSessionRepository
                .findByUserIdAndIsCompletedFalseAndIsExpiredFalse(user.getId());

        return sessions.stream()
                .map(this::mapToSessionDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeHistoryDTO> getPracticeHistory(User user, LocalDateTime startDate, LocalDateTime endDate) {
        List<PracticeSession> sessions = practiceSessionRepository
                .findByUserIdAndStartedAtBetweenAndIsCompletedTrue(user.getId(), startDate, endDate);

        return sessions.stream()
                .map(this::mapToHistoryDTO)
                .toList();
    }

    @Override
    @Transactional
    public void extendSessionTime(String sessionId, int additionalMinutes, User user) {
        PracticeSession session = getSession(sessionId, user);
        validateSessionActive(session);

        session.setExpiresAt(session.getExpiresAt().plusMinutes(additionalMinutes));
        practiceSessionRepository.save(session);

        log.info("Extended session {} by {} minutes", sessionId, additionalMinutes);
        notificationService.sendSessionExtended(user, sessionId, additionalMinutes);
    }

    @Override
    @Transactional
    public void skipQuestion(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);
        validateSessionActive(session);

        // Add current question to answered list (even though skipped, it counts as "done")
        String currentQuestionId = session.getCurrentQuestionId();
        if (currentQuestionId != null && !currentQuestionId.isEmpty()) {
            Set<String> answeredIds = getUsedQuestionIds(session);
            if (!answeredIds.contains(currentQuestionId)) {
                addUsedQuestion(session, currentQuestionId);
            }
        }

        // Mark current question as skipped in history
        addSkippedQuestionToHistory(session);

        // Move to next question index
        session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);

        // Check if session is complete based on unique questions answered/skipped
        Set<String> answeredIds = getUsedQuestionIds(session);
        if (answeredIds.size() >= session.getMaxQuestions()) {
            completeSession(session);
            return;
        }

        // Get next question
        String nextQuestionId = selectNextQuestion(session);
        session.setCurrentQuestionId(nextQuestionId);
        practiceSessionRepository.save(session);

        log.info("Question skipped in session: {}", sessionId);
    }

    @Override
    @Transactional
    public void abandonSession(String sessionId, User user) {
        PracticeSession session = getSession(sessionId, user);

        if (!session.isCompleted()) {
            session.setCompleted(true);
            session.setExpired(true);
            session.setExpiresAt(LocalDateTime.now());
            practiceSessionRepository.save(session);

            log.info("Session {} abandoned by user: {}", sessionId, user.getUsername());
            notificationService.sendSessionAbandoned(user, sessionId);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private PracticeSession getSession(String sessionId, User user) {
        PracticeSession session = practiceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorMessages.SESSION_NO_PERMISSION);
        }

        return session;
    }

    private void validateSessionActive(PracticeSession session) {
        if (session.isCompleted()) {
            throw new BadRequestException(ErrorMessages.SESSION_ALREADY_COMPLETED);
        }

        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setExpired(true);
            practiceSessionRepository.save(session);
            throw new BadRequestException(ErrorMessages.SESSION_EXPIRED);
        }
    }

    private String generateSessionCode() {
        String prefix = appProperties.getPractice().getSessionPrefix();
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Difficulty determineStartingDifficulty(PracticeMatchRequestDTO request, User user) {
        // For MCQ, ignore difficulty preference - questions will be selected adaptively
        if ("MCQ".equalsIgnoreCase(request.getQuestionType())) {
            log.info("MCQ session - using adaptive difficulty selection");
            return Difficulty.MEDIUM; // Default placeholder, won't be used for filtering
        }
        
        // For CODING, use user's difficulty preference
        if (request.getDifficultyPreference() != null &&
                !"ADAPTIVE".equalsIgnoreCase(request.getDifficultyPreference())) {
            try {
                return Difficulty.valueOf(request.getDifficultyPreference().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid difficulty preference: {}, using default", request.getDifficultyPreference());
            }
        }

        // Use configured default difficulty
        String defaultDifficulty = appProperties.getPractice().getDefaultDifficulty();
        return Difficulty.valueOf(defaultDifficulty);
    }

    private PracticeQuestionResponseDTO getFirstQuestion(PracticeSession session) {
        log.info("🎯 Getting first question for session: {}, type: {}, difficulty: {}", 
                session.getId(), session.getQuestionType(), session.getCurrentDifficulty());
        
        String questionId = selectFirstQuestion(session);
        
        if (questionId == null) {
            log.error("❌ No question selected! This should not happen for first question.");
            throw new RuntimeException("No questions available for the selected criteria");
        }
        
        log.info("✅ Selected first question ID: {}", questionId);
        session.setCurrentQuestionId(questionId);
        practiceSessionRepository.save(session);
        
        PracticeQuestionResponseDTO question = questionFetcherService.fetchQuestion(questionId, session.getQuestionType(), session);
        log.info("📝 Fetched question: {}", question != null ? question.getTitle() : "NULL");
        
        return question;
    }

    private PracticeQuestionResponseDTO getNextQuestionForSession(PracticeSession session) {
        session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);
        String nextQuestionId = selectNextQuestion(session);

        if (nextQuestionId == null) {
            completeSession(session);
            return null;
        }

        session.setCurrentQuestionId(nextQuestionId);
        practiceSessionRepository.save(session);
        return questionFetcherService.fetchQuestion(nextQuestionId, session.getQuestionType(), session);
    }

    private String selectFirstQuestion(PracticeSession session) {
        Set<String> usedQuestionIds = getUsedQuestionIds(session);
        log.info("🔍 Selecting first question - Type: {}, Difficulty: {}, Topic: {}, Used IDs: {}", 
                session.getQuestionType(), session.getCurrentDifficulty(), 
                session.getTopic(), usedQuestionIds.size());

        if ("CODING".equals(session.getQuestionType())) {
            List<CodingQuestionDTO> questions = questionFetcherService.getCodingQuestionsByDifficulty(
                    session.getCurrentDifficulty(), session.getTopic(), 20
            );
            log.info("📚 Found {} coding questions", questions != null ? questions.size() : 0);
            return adaptiveQuestionSelector.selectNextCodingQuestion(session, questions, usedQuestionIds);
        } else {
            List<McqQuestionResponseDTO> questions = questionFetcherService.getMcqQuestionsByDifficulty(
                    session.getCurrentDifficulty(), session.getTopic(), 20
            );
            log.info("📚 Found {} MCQ questions for difficulty: {}", 
                    questions != null ? questions.size() : 0, session.getCurrentDifficulty());
            
            String selectedId = adaptiveQuestionSelector.selectNextMcqQuestion(session, questions, usedQuestionIds);
            log.info("🎲 Selected MCQ question ID: {}", selectedId);
            return selectedId;
        }
    }

    private String selectNextQuestion(PracticeSession session) {
        // Adjust difficulty based on performance
        Difficulty newDifficulty = adaptiveQuestionSelector.adjustDifficulty(session);
        if (newDifficulty != session.getCurrentDifficulty()) {
            session.setCurrentDifficulty(newDifficulty);
            log.info("Difficulty adjusted to {} for session: {}", newDifficulty, session.getId());
        }

        Set<String> usedQuestionIds = getUsedQuestionIds(session);

        if ("CODING".equals(session.getQuestionType())) {
            // CODING: Use difficulty-based selection
            List<CodingQuestionDTO> questions = questionFetcherService.getCodingQuestionsByDifficulty(
                    session.getCurrentDifficulty(), session.getTopic(), 20
            );
            return adaptiveQuestionSelector.selectNextCodingQuestion(session, questions, usedQuestionIds);
        } else {
            // MCQ: Use adaptive selection from ALL difficulties
            List<McqQuestionResponseDTO> questions = questionFetcherService.getAllMcqQuestions(
                    session.getTopic(), 50 // Get more questions for better variety
            );
            log.info("📚 Found {} MCQ questions from all difficulties for adaptive selection", 
                    questions != null ? questions.size() : 0);
            return adaptiveQuestionSelector.selectNextMcqQuestion(session, questions, usedQuestionIds);
        }
    }

    private boolean processSubmission(PracticeSubmissionDTO submission, PracticeSession session) {
        // Add question to used list (only if not already there)
        Set<String> answeredIds = getUsedQuestionIds(session);
        boolean isFirstAttempt = !answeredIds.contains(submission.getQuestionId());
        
        if (isFirstAttempt) {
            addUsedQuestion(session, submission.getQuestionId());
            session.setTotalQuestionsAnswered(session.getTotalQuestionsAnswered() + 1);
        }

        boolean isCorrect = false;

        if ("CODING".equals(submission.getQuestionType())) {
            CodeExecutionDTO codeExecutionDTO = new CodeExecutionDTO();
            codeExecutionDTO.setCodingQuestionId(submission.getQuestionId());
            codeExecutionDTO.setCode(submission.getSourceCode());
            codeExecutionDTO.setLanguage(submission.getLanguage());
            codeExecutionDTO.setVersion(submissionService.getLanguageVersion(submission.getLanguage()));
            CodeExecutionResultDTO codeExecutionResultDTO = codeExecutionService.submitCode(codeExecutionDTO);
            log.info("Output of coding practice submission: {}", codeExecutionResultDTO);
            
            if (codeExecutionResultDTO != null && "✅ Accepted".equals(codeExecutionResultDTO.getStdout())) {
                isCorrect = true;
            }
//            CodingEvaluationResult evaluationResult = evaluateCodingSubmission(submission, session);
//            isCorrect = evaluationResult.isPassed();
//            storeCodingEvaluation(session, submission, evaluationResult);
        } else {
            isCorrect = evaluateMcqSubmission(submission, session);
        }

        // Update statistics
        if (isCorrect) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }

        updateAverageTime(session, submission.getTimeTakenSeconds());
        session.setAccuracyPercentage(
                (double) session.getCorrectAnswers() / session.getTotalQuestionsAnswered() * 100
        );

        // Add to performance history
        addPerformanceHistory(session, submission, isCorrect);

        // Save session
        practiceSessionRepository.save(session);

        return isCorrect;
    }

    private CodingEvaluationResult evaluateCodingSubmission(PracticeSubmissionDTO submission, PracticeSession session) {
        try {
            CodingQuestion codingQuestion = codingQuestionRepository.findById(submission.getQuestionId()).get();
            int timeLimit = (int) codingQuestion.getTimeLimit();
            CodeExecutionDTO codeExecutionDTO = new CodeExecutionDTO();
            codeExecutionDTO.setCodingQuestionId(submission.getQuestionId());
            codeExecutionDTO.setCode(submission.getSourceCode());
            codeExecutionDTO.setLanguage(submission.getLanguage());
            codeExecutionDTO.setVersion(submissionService.getLanguageVersion(submission.getLanguage()));
            CodeExecutionResultDTO codeExecutionResultDTO =codeExecutionService.submitCode(codeExecutionDTO);
            CodingEvaluationResult codingEvaluationResult = new CodingEvaluationResult();
            return null;
//            return codeExecutionService.submitCode(codeExecutionDTO);
        } catch (Exception e) {
            log.error("Error evaluating coding submission: {}", e.getMessage(), e);
            return CodingEvaluationResult.builder()
                    .passed(false)
                    .message("Evaluation failed: " + e.getMessage())
                    .totalTestCases(0)
                    .passedTestCases(0)
                    .build();
        }
    }

    private boolean evaluateMcqSubmission(PracticeSubmissionDTO submission, PracticeSession session) {
        try {
            McqSubmissionResultDTO evaluationResult = mcqEvaluationService.evaluateMcqSubmission(
                    submission.getQuestionId(),
                    submission.getSelectedOptionId(),
                    submission.getTimeTakenSeconds()
            );

            storeMcqEvaluation(session, submission, evaluationResult);
            return evaluationResult.isCorrect();
        } catch (Exception e) {
            log.error("Error evaluating MCQ submission: {}", e.getMessage(), e);
            return false;
        }
    }

    private void storeCodingEvaluation(PracticeSession session,
                                       PracticeSubmissionDTO submission,
                                       CodingEvaluationResult evaluationResult) {
        Map<String, String> metrics = session.getPerformanceMetrics();
        if (metrics == null) {
            metrics = new HashMap<>();
        }

        String keyPrefix = "coding_q" + session.getTotalQuestionsAnswered() + "_";
        metrics.put(keyPrefix + "total_tests", String.valueOf(evaluationResult.getTotalTestCases()));
        metrics.put(keyPrefix + "passed_tests", String.valueOf(evaluationResult.getPassedTestCases()));
        metrics.put(keyPrefix + "avg_time", String.valueOf(evaluationResult.getAverageExecutionTime()));
        metrics.put(keyPrefix + "max_memory", String.valueOf(evaluationResult.getMaxMemoryUsed()));

        if (evaluationResult.getCompilationError() != null) {
            metrics.put(keyPrefix + "compilation_error", "true");
        }

        session.setPerformanceMetrics(metrics);
    }

    private void storeMcqEvaluation(PracticeSession session,
                                    PracticeSubmissionDTO submission,
                                    McqSubmissionResultDTO evaluationResult) {
        Map<String, String> metrics = session.getPerformanceMetrics();
        if (metrics == null) {
            metrics = new HashMap<>();
        }

        String keyPrefix = "mcq_q" + session.getTotalQuestionsAnswered() + "_";
        metrics.put(keyPrefix + "correct", String.valueOf(evaluationResult.isCorrect()));
        metrics.put(keyPrefix + "time_taken", String.valueOf(evaluationResult.getTimeTakenSeconds()));
        metrics.put(keyPrefix + "time_confidence", String.valueOf(evaluationResult.getTimeConfidenceScore()));
        metrics.put(keyPrefix + "difficulty", evaluationResult.getDifficultyLevel());

        if (evaluationResult.getTags() != null && !evaluationResult.getTags().isEmpty()) {
            metrics.put(keyPrefix + "topics", String.join(",", evaluationResult.getTags()));
        }

        session.setPerformanceMetrics(metrics);
    }

    private void addPerformanceHistory(PracticeSession session,
                                       PracticeSubmissionDTO submission,
                                       boolean isCorrect) {
        try {
            List<Map<String, Object>> history = objectMapper.readValue(
                    session.getPerformanceHistory(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            Map<String, Object> entry = new HashMap<>();
            entry.put("questionId", submission.getQuestionId());
            entry.put("questionType", submission.getQuestionType());
            entry.put("timeTaken", submission.getTimeTakenSeconds());
            entry.put("isCorrect", isCorrect);
            entry.put("timestamp", LocalDateTime.now().toString());
            entry.put("confidence", submission.getConfidenceScore());

            if ("CODING".equals(submission.getQuestionType())) {
                entry.put("language", submission.getLanguage());
                if (submission.getTestCasesPassed() > 0) {
                    entry.put("testCasesPassed", submission.getTestCasesPassed());
                    entry.put("totalTestCases", submission.getTotalTestCases());
                }
                if (submission.getSourceCode() != null) {
                    // Safe truncation that won't break JSON
                    String codeSnippet = truncateCodeSafely(submission.getSourceCode(), 500);
                    entry.put("codeSnippet", codeSnippet);
                }
            } else {
                entry.put("selectedOptionId", submission.getSelectedOptionId());
            }

            history.add(entry);
            session.setPerformanceHistory(objectMapper.writeValueAsString(history));

        } catch (JsonProcessingException e) {
            log.error("Error updating performance history: {}", e.getMessage(), e);
        }
    }

    private void addSkippedQuestionToHistory(PracticeSession session) {
        try {
            List<Map<String, Object>> history = objectMapper.readValue(
                    session.getPerformanceHistory(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            Map<String, Object> entry = new HashMap<>();
            entry.put("questionId", session.getCurrentQuestionId());
            entry.put("questionType", session.getQuestionType());
            entry.put("timeTaken", 0);
            entry.put("isCorrect", false);
            entry.put("timestamp", LocalDateTime.now().toString());
            entry.put("confidence", 0.0);
            entry.put("skipped", true);

            history.add(entry);
            session.setPerformanceHistory(objectMapper.writeValueAsString(history));

        } catch (JsonProcessingException e) {
            log.error("Error updating skipped question history: {}", e.getMessage(), e);
        }
    }

    private void completeSession(PracticeSession session) {
        session.setCompleted(true);
        session.setExpiresAt(LocalDateTime.now());
        practiceSessionRepository.save(session);

        log.info("Practice session completed: {}", session.getId());

        // Send completion notification
        notificationService.sendPracticeSessionCompleted(session.getUser(), session.getId(), null);
    }

    private Set<String> getUsedQuestionIds(PracticeSession session) {
        try {
            List<String> usedIds = objectMapper.readValue(
                    session.getAnsweredQuestionIds(),
                    new TypeReference<List<String>>() {}
            );
            return new HashSet<>(usedIds);
        } catch (JsonProcessingException e) {
            log.error("Error parsing used question IDs: {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    private void addUsedQuestion(PracticeSession session, String questionId) {
        try {
            List<String> usedIds = objectMapper.readValue(
                    session.getAnsweredQuestionIds(),
                    new TypeReference<List<String>>() {}
            );
            usedIds.add(questionId);
            session.setAnsweredQuestionIds(objectMapper.writeValueAsString(usedIds));
        } catch (JsonProcessingException e) {
            log.error("Error updating used question IDs: {}", e.getMessage(), e);
        }
    }

    private void updateAverageTime(PracticeSession session, long newTime) {
        double currentTotal = session.getAverageTimePerQuestion() * (session.getTotalQuestionsAnswered() - 1);
        session.setAverageTimePerQuestion((currentTotal + newTime) / session.getTotalQuestionsAnswered());
    }

    private long calculateTotalTime(PracticeSession session) {
        return Duration.between(session.getStartedAt(), session.getExpiresAt()).getSeconds();
    }

    private String calculateOverallDifficulty(PracticeSession session) {
        return session.getCurrentDifficulty().name();
    }

    private String generateAIFeedback(PracticeSession session) {
        double accuracy = session.getAccuracyPercentage();
        String feedback;

        if (accuracy >= 90) {
            feedback = "Excellent performance! You're demonstrating mastery in this area.";
        } else if (accuracy >= 75) {
            feedback = "Great work! You have a solid understanding of the concepts.";
        } else if (accuracy >= 60) {
            feedback = "Good effort! You're on the right track, keep practicing to improve.";
        } else {
            feedback = "Keep practicing! Focus on understanding the concepts and reviewing mistakes.";
        }

        return String.format(
                "You completed %d/%d questions with %.1f%% accuracy. Average time per question: %.1f seconds. %s",
                session.getCorrectAnswers(),
                session.getTotalQuestionsAnswered(),
                accuracy,
                session.getAverageTimePerQuestion(),
                feedback
        );
    }

    private SubmissionResultDTO buildSubmissionResult(PracticeSubmissionDTO submission,
                                                      PracticeSession session,
                                                      boolean isCorrect) {
        return SubmissionResultDTO.builder()
                .questionId(submission.getQuestionId())
                .questionType(submission.getQuestionType())
                .isCorrect(isCorrect)
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .sessionProgress(session.getCurrentQuestionIndex() + 1)
                .totalQuestions(session.getMaxQuestions())
                .correctAnswers(session.getCorrectAnswers())
                .accuracyPercentage(session.getAccuracyPercentage())
                .message(isCorrect ? "Correct answer!" : "Incorrect answer. Try again!")
                .nextQuestionAvailable(session.getCurrentQuestionIndex() < session.getMaxQuestions() - 1)
                .build();
    }

    private PracticeSessionDTO mapToSessionDTO(PracticeSession session) {
        return PracticeSessionDTO.builder()
                .sessionId(session.getId())
                .userId(session.getUser().getId())
                .sessionCode(session.getSessionCode())
                .questionType(session.getQuestionType())
                .topic(session.getTopic())
//                .startingDifficulty(session.getStartingDifficulty())
                .currentDifficulty(session.getCurrentDifficulty())
                .maxQuestions(session.getMaxQuestions())
                .timeLimitMinutes(session.getTimeLimitMinutes())
                .startedAt(session.getStartedAt())
                .expiresAt(session.getExpiresAt())
                .remainingTimeMinutes(getRemainingTime(session))
                .currentQuestionNumber(session.getCurrentQuestionIndex() + 1)
                .totalQuestionsAnswered(session.getTotalQuestionsAnswered())
                .correctAnswers(session.getCorrectAnswers())
                .averageTimePerQuestion(session.getAverageTimePerQuestion())
                .accuracyPercentage(session.getAccuracyPercentage())
                .currentQuestionId(session.getCurrentQuestionId())
                .performanceMetrics(session.getPerformanceMetrics())
                .isCompleted(session.isCompleted())
                .isExpired(session.isExpired())
                .build();
    }

    private PracticeHistoryDTO mapToHistoryDTO(PracticeSession session) {
        return PracticeHistoryDTO.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .questionType(session.getQuestionType())
                .topic(session.getTopic())
                .difficulty(session.getCurrentDifficulty())
                .totalQuestions(session.getMaxQuestions())
                .questionsAnswered(session.getTotalQuestionsAnswered())
                .correctAnswers(session.getCorrectAnswers())
                .accuracyPercentage(session.getAccuracyPercentage())
                .totalTimeTakenSeconds(calculateTotalTime(session))
                .startedAt(session.getStartedAt())
                .completedAt(session.getExpiresAt())
                .build();
    }

    private long getRemainingTime(PracticeSession session) {
        if (session.isCompleted() || session.isExpired()) {
            return 0;
        }

        long remainingSeconds = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        return Math.max(0, remainingSeconds / 60); // Convert to minutes
    }

    /**
     * ✅ Safe code truncation that preserves JSON integrity
     */
    private String truncateCodeSafely(String code, int maxLength) {
        if (code == null) return "";
        if (code.length() <= maxLength) return code;

        // Truncate
        String truncated = code.substring(0, maxLength);

        // Remove trailing backslash (incomplete escape sequence)
        while (truncated.endsWith("\\") && truncated.length() > 0) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }

        // Balance quotes to prevent JSON parsing errors
        long doubleQuoteCount = truncated.chars().filter(ch -> ch == '"').count();
        if (doubleQuoteCount % 2 != 0) {
            int lastQuote = truncated.lastIndexOf('"');
            if (lastQuote > 0) {
                truncated = truncated.substring(0, lastQuote);
            }
        }

        // Remove trailing newlines/whitespace
        truncated = truncated.trim();

        return truncated + "\n... [code truncated]";
    }

    /**
     * ✅ Auto-cleanup expired sessions (can be called by scheduled task)
     */
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<PracticeSession> expiredSessions = practiceSessionRepository
                .findByIsCompletedFalseAndIsExpiredFalseAndStartedAtBefore(cutoffTime);

        for (PracticeSession session : expiredSessions) {
            session.setExpired(true);
            session.setCompleted(true);
            log.info("Auto-cleaned expired session: {}", session.getId());
        }

        practiceSessionRepository.saveAll(expiredSessions);
    }
}