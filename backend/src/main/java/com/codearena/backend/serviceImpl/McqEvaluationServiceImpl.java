package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.service.McqEvaluationService;
import com.codearena.backend.service.McqQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class McqEvaluationServiceImpl implements McqEvaluationService {

    private final McqQuestionService mcqQuestionService;

    /**
     * Evaluate MCQ submission
     */
    public McqSubmissionResultDTO evaluateMcqSubmission(
            String questionId,
            String selectedOptionId,
            long timeTakenSeconds) {

        try {
            // Get question with correct answers
            McqQuestionResponseDTO question = mcqQuestionService.getById(questionId);

            // Find the selected option
            boolean isCorrect = question.getOptions().stream()
                    .filter(option -> option.getId().equals(selectedOptionId))
                    .findFirst()
                    .map(option -> option.isCorrect())
                    .orElse(false);

            // Find correct option(s) - some questions may have multiple correct
            List<String> correctOptionIds = question.getOptions().stream()
                    .filter(option -> option.isCorrect())
                    .map(option -> option.getId())
                    .toList();

            // Calculate confidence score based on time taken
            double timeConfidence = calculateTimeConfidence(timeTakenSeconds, question.getTimeLimit());

            // Determine question difficulty
            String difficultyLevel = question.getDifficulty() != null ?
                    question.getDifficulty().name() : "MEDIUM";

            return McqSubmissionResultDTO.builder()
                    .questionId(questionId)
                    .selectedOptionId(selectedOptionId)
                    .isCorrect(isCorrect)
                    .correctOptionIds(correctOptionIds)
                    .explanation(generateExplanation(question, selectedOptionId, isCorrect))
                    .timeTakenSeconds(timeTakenSeconds)
                    .timeConfidenceScore(timeConfidence)
                    .difficultyLevel(difficultyLevel)
                    .tags(question.getTags())
                    .pointsEarned(isCorrect ? question.getPoints() : 0)
                    .submittedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error evaluating MCQ submission: {}", e.getMessage(), e);
            return McqSubmissionResultDTO.builder()
                    .questionId(questionId)
                    .selectedOptionId(selectedOptionId)
                    .isCorrect(false)
                    .explanation("Evaluation failed: " + e.getMessage())
                    .timeTakenSeconds(timeTakenSeconds)
                    .timeConfidenceScore(0.0)
                    .difficultyLevel("UNKNOWN")
                    .pointsEarned(0)
                    .submittedAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Evaluate multiple MCQ submissions (for batch processing)
     */
    public List<McqSubmissionResultDTO> evaluateBatchMcqSubmissions(
            Map<String, String> questionIdToSelectedOption,
            Map<String, Long> questionIdToTimeTaken) {

        return questionIdToSelectedOption.entrySet().stream()
                .map(entry -> {
                    String questionId = entry.getKey();
                    String selectedOptionId = entry.getValue();
                    long timeTaken = questionIdToTimeTaken.getOrDefault(questionId, 0L);

                    return evaluateMcqSubmission(questionId, selectedOptionId, timeTaken);
                })
                .toList();
    }

    /**
     * Analyze MCQ performance patterns
     */
    public McqPerformanceAnalysis analyzeMcqPerformance(
            PracticeSession session,
            List<McqSubmissionResultDTO> submissionHistory) {

        if (submissionHistory.isEmpty()) {
            return McqPerformanceAnalysis.builder()
                    .totalQuestions(0)
                    .correctAnswers(0)
                    .accuracy(0.0)
                    .averageTimePerQuestion(0.0)
                    .weakTopics(List.of())
                    .strongTopics(List.of())
                    .build();
        }

        int totalQuestions = submissionHistory.size();
        int correctAnswers = (int) submissionHistory.stream()
                .filter(McqSubmissionResultDTO::isCorrect)
                .count();

        double accuracy = (double) correctAnswers / totalQuestions * 100;

        double averageTime = submissionHistory.stream()
                .mapToLong(McqSubmissionResultDTO::getTimeTakenSeconds)
                .average()
                .orElse(0.0);

        // Analyze topic-wise performance
        Map<String, TopicPerformance> topicPerformance = analyzeTopicPerformance(submissionHistory);

        // Identify weak and strong topics
        List<String> weakTopics = topicPerformance.entrySet().stream()
                .filter(entry -> entry.getValue().getAccuracy() < 60)
                .map(Map.Entry::getKey)
                .toList();

        List<String> strongTopics = topicPerformance.entrySet().stream()
                .filter(entry -> entry.getValue().getAccuracy() >= 80)
                .map(Map.Entry::getKey)
                .toList();

        // Analyze difficulty-wise performance
        Map<String, DifficultyPerformance> difficultyPerformance = analyzeDifficultyPerformance(submissionHistory);

        return McqPerformanceAnalysis.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .accuracy(accuracy)
                .averageTimePerQuestion(averageTime)
                .weakTopics(weakTopics)
                .strongTopics(strongTopics)
                .topicPerformance(topicPerformance)
                .difficultyPerformance(difficultyPerformance)
                .recommendations(generateRecommendations(topicPerformance, difficultyPerformance))
                .build();
    }

    /**
     * Generate personalized explanations for MCQ answers
     */
    public String generatePersonalizedExplanation(
            String questionId,
            String selectedOptionId,
            boolean isCorrect,
            List<McqSubmissionResultDTO> previousSubmissions) {

        try {
            McqQuestionResponseDTO question = mcqQuestionService.getById(questionId);

            // Find the correct explanation
            String correctExplanation = findCorrectExplanation(question);

            // Find why the selected option is wrong (if incorrect)
            String mistakeAnalysis = "";
            if (!isCorrect) {
                mistakeAnalysis = analyzeCommonMistake(question, selectedOptionId, previousSubmissions);
            }

            // Generate learning tip
            String learningTip = generateLearningTip(question, isCorrect);

            return String.format("%s\n\n%s\n\n%s",
                    isCorrect ? "Correct! " + correctExplanation : "Incorrect. " + mistakeAnalysis,
                    correctExplanation,
                    learningTip);

        } catch (Exception e) {
            log.error("Error generating explanation: {}", e.getMessage());
            return isCorrect ? "Correct answer!" : "Incorrect answer.";
        }
    }

    /**
     * Get next MCQ question recommendation based on performance
     */
    public String recommendNextMcqQuestion(
            PracticeSession session,
            List<McqSubmissionResultDTO> submissionHistory,
            List<String> availableQuestionIds) {

        // Analyze performance to identify weak areas
        McqPerformanceAnalysis analysis = analyzeMcqPerformance(session, submissionHistory);

        // If we have weak topics, prioritize questions from those topics
        if (!analysis.getWeakTopics().isEmpty()) {
            // Try to find questions from weak topics
            for (String weakTopic : analysis.getWeakTopics()) {
                for (String questionId : availableQuestionIds) {
                    try {
                        McqQuestionResponseDTO question = mcqQuestionService.getById(questionId);
                        if (question.getTags() != null &&
                                question.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(weakTopic))) {
                            return questionId;
                        }
                    } catch (Exception e) {
                        // Skip if question not found
                    }
                }
            }
        }

        // If no weak topics or no matching questions, return first available
        return availableQuestionIds.isEmpty() ? null : availableQuestionIds.get(0);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private double calculateTimeConfidence(long timeTaken, int timeLimit) {
        if (timeLimit <= 0) return 1.0;

        double timeRatio = (double) timeTaken / timeLimit;

        if (timeRatio <= 0.3) return 0.9; // Very fast
        if (timeRatio <= 0.6) return 0.7; // Fast
        if (timeRatio <= 0.9) return 0.5; // Moderate
        if (timeRatio <= 1.0) return 0.3; // Slow but within limit
        return 0.1; // Exceeded time limit
    }

    private String generateExplanation(
            McqQuestionResponseDTO question,
            String selectedOptionId,
            boolean isCorrect) {

        if (isCorrect) {
            return "Your answer is correct! " +
                    (question.getDescription() != null ?
                            "Explanation: " + question.getDescription() :
                            "Well done!");
        } else {
            // Find why the selected option is wrong
            String selectedOptionText = question.getOptions().stream()
                    .filter(option -> option.getId().equals(selectedOptionId))
                    .findFirst()
                    .map(option -> option.getOptionText())
                    .orElse("Selected option");

            String correctOptionText = question.getOptions().stream()
                    .filter(option -> option.isCorrect())
                    .findFirst()
                    .map(option -> option.getOptionText())
                    .orElse("Correct option");

            return String.format(
                    "You selected: '%s'\nCorrect answer: '%s'\n\nExplanation: %s",
                    truncateText(selectedOptionText, 50),
                    truncateText(correctOptionText, 50),
                    question.getDescription() != null ?
                            question.getDescription() : "Review the concept."
            );
        }
    }

    private Map<String, TopicPerformance> analyzeTopicPerformance(
            List<McqSubmissionResultDTO> submissions) {

        Map<String, TopicPerformance> topicMap = new HashMap<>();

        for (McqSubmissionResultDTO submission : submissions) {
            if (submission.getTags() != null) {
                for (String tag : submission.getTags()) {
                    TopicPerformance performance = topicMap.getOrDefault(tag,
                            TopicPerformance.builder()
                                    .topic(tag)
                                    .totalQuestions(0)
                                    .correctAnswers(0)
                                    .totalTimeSeconds(0)
                                    .build());

                    performance.setTotalQuestions(performance.getTotalQuestions() + 1);
                    if (submission.isCorrect()) {
                        performance.setCorrectAnswers(performance.getCorrectAnswers() + 1);
                    }
                    performance.setTotalTimeSeconds(performance.getTotalTimeSeconds() +
                            submission.getTimeTakenSeconds());

                    topicMap.put(tag, performance);
                }
            }
        }

        // Calculate accuracy and average time for each topic
        topicMap.values().forEach(performance -> {
            performance.setAccuracy(
                    (double) performance.getCorrectAnswers() / performance.getTotalQuestions() * 100
            );
            performance.setAverageTime(
                    (double) performance.getTotalTimeSeconds() / performance.getTotalQuestions()
            );
        });

        return topicMap;
    }

    private Map<String, DifficultyPerformance> analyzeDifficultyPerformance(
            List<McqSubmissionResultDTO> submissions) {

        Map<String, DifficultyPerformance> difficultyMap = new HashMap<>();

        for (McqSubmissionResultDTO submission : submissions) {
            String difficulty = submission.getDifficultyLevel();
            DifficultyPerformance performance = difficultyMap.getOrDefault(difficulty,
                    DifficultyPerformance.builder()
                            .difficulty(difficulty)
                            .totalQuestions(0)
                            .correctAnswers(0)
                            .totalTimeSeconds(0)
                            .build());

            performance.setTotalQuestions(performance.getTotalQuestions() + 1);
            if (submission.isCorrect()) {
                performance.setCorrectAnswers(performance.getCorrectAnswers() + 1);
            }
            performance.setTotalTimeSeconds(performance.getTotalTimeSeconds() +
                    submission.getTimeTakenSeconds());

            difficultyMap.put(difficulty, performance);
        }

        // Calculate metrics
        difficultyMap.values().forEach(performance -> {
            performance.setAccuracy(
                    (double) performance.getCorrectAnswers() / performance.getTotalQuestions() * 100
            );
            performance.setAverageTime(
                    (double) performance.getTotalTimeSeconds() / performance.getTotalQuestions()
            );
        });

        return difficultyMap;
    }

    private String findCorrectExplanation(McqQuestionResponseDTO question) {
        // Look for explanation in question description or options
        if (question.getDescription() != null && !question.getDescription().isBlank()) {
            return truncateText(question.getDescription(), 200);
        }

        // Find first correct option for explanation
        return question.getOptions().stream()
                .filter(option -> option.isCorrect())
                .findFirst()
                .map(option -> "Correct option: " + truncateText(option.getOptionText(), 100))
                .orElse("The selected option is correct.");
    }

    private String analyzeCommonMistake(
            McqQuestionResponseDTO question,
            String selectedOptionId,
            List<McqSubmissionResultDTO> previousSubmissions) {

        // Check if this is a common mistake pattern
        long similarMistakes = previousSubmissions.stream()
                .filter(sub -> !sub.isCorrect())
                .filter(sub -> sub.getQuestionId().equals(question.getId()) ||
                        (sub.getTags() != null && question.getTags() != null &&
                                sub.getTags().stream().anyMatch(tag -> question.getTags().contains(tag))))
                .count();

        if (similarMistakes > 2) {
            return "You've made similar mistakes before. Consider reviewing this concept more thoroughly.";
        }

        return "Review the fundamental concept to understand why this option is incorrect.";
    }

    private String generateLearningTip(McqQuestionResponseDTO question, boolean isCorrect) {
        if (isCorrect) {
            if (question.getTags() != null && !question.getTags().isEmpty()) {
                return "Great! Now try more advanced questions on: " +
                        String.join(", ", question.getTags());
            }
            return "Good work! Keep practicing similar questions.";
        } else {
            if (question.getTags() != null && !question.getTags().isEmpty()) {
                return "Tip: Study the concept of " + question.getTags().get(0) +
                        " before attempting similar questions.";
            }
            return "Tip: Read the question carefully and eliminate obviously wrong options first.";
        }
    }

    private String generateRecommendations(
            Map<String, TopicPerformance> topicPerformance,
            Map<String, DifficultyPerformance> difficultyPerformance) {

        StringBuilder recommendations = new StringBuilder();

        // Topic-based recommendations
        topicPerformance.entrySet().stream()
                .filter(entry -> entry.getValue().getAccuracy() < 60)
                .forEach(entry -> {
                    recommendations.append(String.format(
                            "• Focus on '%s' (Accuracy: %.1f%%)\n",
                            entry.getKey(), entry.getValue().getAccuracy()
                    ));
                });

        // Difficulty-based recommendations
        difficultyPerformance.entrySet().stream()
                .filter(entry -> entry.getValue().getAccuracy() < 50)
                .forEach(entry -> {
                    recommendations.append(String.format(
                            "• Practice more '%s' difficulty questions\n",
                            entry.getKey()
                    ));
                });

        if (recommendations.length() == 0) {
            recommendations.append("• Continue practicing to maintain your skills");
        }

        return recommendations.toString();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}