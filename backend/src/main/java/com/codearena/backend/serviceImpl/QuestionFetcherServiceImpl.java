package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.service.CodingQuestionService;
import com.codearena.backend.service.McqQuestionService;
import com.codearena.backend.service.QuestionFetcherService;
import com.codearena.backend.utils.constant.Difficulty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionFetcherServiceImpl implements QuestionFetcherService {

    private final CodingQuestionService codingQuestionService;
    private final McqQuestionService mcqQuestionService;

    public PracticeQuestionResponseDTO fetchQuestion(String questionId, String questionType, PracticeSession session) {
        if ("CODING".equals(questionType)) {
            return fetchCodingQuestion(questionId, session);
        } else if ("MCQ".equals(questionType)) {
            return fetchMcqQuestion(questionId, session);
        } else {
            throw new IllegalArgumentException("Invalid question type: " + questionType);
        }
    }

    private PracticeQuestionResponseDTO fetchCodingQuestion(String questionId, PracticeSession session) {
        try {
            CodingQuestionDTO codingQuestion = codingQuestionService.getQuestionById(questionId);

            return PracticeQuestionResponseDTO.builder()
                    .sessionId(session.getId())
                    .questionId(codingQuestion.getId())
                    .questionType("CODING")
                    .title(codingQuestion.getTitle())
                    .description(codingQuestion.getDescription())
                    .difficulty(codingQuestion.getDifficulty())
                    .questionNumber(session.getCurrentQuestionIndex() + 1)
                    .totalQuestions(session.getMaxQuestions())
                    .timeRemainingSeconds(calculateTimeRemaining(session))
                    .isLastQuestion(session.getCurrentQuestionIndex() >= session.getMaxQuestions() - 1)
                    .inputFormat(codingQuestion.getInputFormat())
                    .outputFormat(codingQuestion.getOutputFormat())
                    .constraints(codingQuestion.getConstraints())
                    .starterCodes(codingQuestion.getStarterCodes())
                    .sampleTestCases(getSampleTestCases(codingQuestion.getTestCases()))
                    .tags(codingQuestion.getTags())
                    .points(codingQuestion.getPoints())
                    .timeLimit((int) codingQuestion.getTimeLimit())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching coding question {}: {}", questionId, e.getMessage());
            throw new RuntimeException("Failed to fetch coding question: " + e.getMessage());
        }
    }

    private PracticeQuestionResponseDTO fetchMcqQuestion(String questionId, PracticeSession session) {
        try {
            McqQuestionResponseDTO mcqQuestion = mcqQuestionService.getById(questionId);

            // Sanitize options - hide correct answers
            List<McqOptionJsonDTO> sanitizedOptions = mcqQuestion.getOptions().stream()
                    .map(option -> new McqOptionJsonDTO(
                            option.getId(),
                            option.getOptionText(),
                            false // Hide correct answer from client
                    ))
                    .toList();

            return PracticeQuestionResponseDTO.builder()
                    .sessionId(session.getId())
                    .questionId(mcqQuestion.getId())
                    .questionType("MCQ")
                    .title(mcqQuestion.getTitle())
                    .description(mcqQuestion.getDescription())
                    .difficulty(mcqQuestion.getDifficulty())
                    .questionNumber(session.getCurrentQuestionIndex() + 1)
                    .totalQuestions(session.getMaxQuestions())
                    .timeRemainingSeconds(calculateTimeRemaining(session))
                    .isLastQuestion(session.getCurrentQuestionIndex() >= session.getMaxQuestions() - 1)
                    .options(sanitizedOptions)
                    .tags(mcqQuestion.getTags())
                    .points(mcqQuestion.getPoints())
                    .timeLimit(mcqQuestion.getTimeLimit())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching MCQ question {}: {}", questionId, e.getMessage());
            throw new RuntimeException("Failed to fetch MCQ question: " + e.getMessage());
        }
    }

    public List<CodingQuestionDTO> getCodingQuestionsByDifficulty(Difficulty difficulty, String topic, int count) {
        if (topic != null && !topic.trim().isEmpty()) {
            return codingQuestionService.getByDifficultyAndTopicAndCount(difficulty.name(), topic, count);
        }
        return codingQuestionService.getByDifficultyAndCount(difficulty.name(), count);
    }

    public List<McqQuestionResponseDTO> getMcqQuestionsByDifficulty(Difficulty difficulty, String topic, int count) {
        if (topic != null && !topic.trim().isEmpty()) {
            return mcqQuestionService.getByDifficultyAndTopicAndCount(difficulty.name(), topic, count);
        }
        return mcqQuestionService.getByDifficultyAndCount(difficulty.name(), count);
    }

    @Override
    public List<McqQuestionResponseDTO> getAllMcqQuestions(String topic, int count) {
        // Get MCQ questions from all difficulty levels for adaptive selection
        if (topic != null && !topic.trim().isEmpty()) {
            // If topic specified, get mixed difficulty questions for that topic
            return mcqQuestionService.getByDifficultyAndTopicAndCount("MEDIUM", topic, count);
        }
        // Get mixed difficulty questions (all levels)
        return mcqQuestionService.getMixedDifficultyQuestions(count);
    }

    private List<TestCaseJsonDTO> getSampleTestCases(List<TestCaseJsonDTO> allTestCases) {
        return allTestCases.stream()
                .filter(TestCaseJsonDTO::isSample)
                .toList();
    }

    private long calculateTimeRemaining(PracticeSession session) {
        return Math.max(0, java.time.Duration.between(
                java.time.LocalDateTime.now(), session.getExpiresAt()
        ).getSeconds());
    }
}