package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.CodingQuestionDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;
import com.codearena.backend.dto.RecommendationsDTO;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.service.ai.AdaptiveQuestionSelector;
import com.codearena.backend.utils.constant.Difficulty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AdaptiveQuestionSelectorImpl implements AdaptiveQuestionSelector {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AdaptiveQuestionSelectorImpl(@Qualifier("openAiChatModel") ChatModel chatModel, 
                                        ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    /**
     * AI-powered question selection based on user performance
     */
    public String selectNextCodingQuestion(PracticeSession session,
                                           List<CodingQuestionDTO> candidateQuestions,
                                           Set<String> usedQuestionIds) {

        // Prepare performance context for AI
        String performanceContext = buildPerformanceContext(session);

        // Filter out used questions
        List<CodingQuestionDTO> availableQuestions = candidateQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            log.warn("No new questions available for session: {}", session.getId());
            return null;
        }

        // Try AI selection first, fallback to random if AI fails (quota, timeout, etc.)
        try {
            log.info("🤖 Attempting AI-powered coding question selection...");
            
            String prompt = """
                Student: %s
                Questions (%d): %s
                Select best question ID based on performance. Return ONLY the ID.
                """.formatted(
                    performanceContext,
                    availableQuestions.size(),
                    buildQuestionsSummary(availableQuestions)
            );

            ChatClient chatClient = ChatClient.create(chatModel);
            String selectedQuestionId = chatClient.prompt()
                    .system("Return ONLY the question ID.")
                    .user(prompt)
                    .call()
                    .content()
                    .trim();

            log.info("✅ AI selected coding question ID: {}", selectedQuestionId);
            return selectedQuestionId;
            
        } catch (Exception e) {
            log.warn("⚠️ AI selection failed ({}), falling back to random selection", e.getMessage());
            
            // Fallback: Random selection
            CodingQuestionDTO randomQuestion = availableQuestions.get(
                    new Random().nextInt(availableQuestions.size())
            );
            
            log.info("🎲 Randomly selected coding question ID: {}", randomQuestion.getId());
            return randomQuestion.getId();
        }
    }

    /**
     * Select next MCQ question using AI
     */
    public String selectNextMcqQuestion(PracticeSession session,
                                        List<McqQuestionResponseDTO> candidateQuestions,
                                        Set<String> usedQuestionIds) {

        String performanceContext = buildPerformanceContext(session);

        List<McqQuestionResponseDTO> availableQuestions = candidateQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            log.warn("No new MCQ questions available for session: {}", session.getId());
            return null;
        }

        // Try AI selection first, fallback to random if AI fails
        try {
            log.info("🤖 Attempting AI-powered adaptive MCQ question selection...");
            
            // Enhanced prompt for adaptive selection based on performance
            String prompt = """
                Student Performance: %s
                Available Questions (%d): %s
                
                Select the BEST next question based on adaptive learning:
                1. If accuracy > 80%% → Select HARD difficulty questions
                2. If accuracy 50-80%% → Mix MEDIUM and HARD questions
                3. If accuracy < 50%% → Select EASY or MEDIUM questions
                4. If slow response time → Select easier questions
                5. Ensure variety in topics and difficulty
                
                Return ONLY the question ID.
                """.formatted(
                    performanceContext,
                    availableQuestions.size(),
                    buildMcqQuestionsSummary(availableQuestions)
            );

            ChatClient chatClient = ChatClient.create(chatModel);
            String selectedQuestionId = chatClient.prompt()
                    .system("You are an adaptive learning AI. Return ONLY the question ID.")
                    .user(prompt)
                    .call()
                    .content()
                    .trim();
            
            log.info("✅ AI selected adaptive MCQ question ID: {}", selectedQuestionId);
            return selectedQuestionId;
            
        } catch (Exception e) {
            log.warn("⚠️ AI selection failed ({}), falling back to random selection", e.getMessage());
            
            // Fallback: Random selection from all difficulties
            McqQuestionResponseDTO randomQuestion = availableQuestions.get(
                    new Random().nextInt(availableQuestions.size())
            );
            
            log.info("🎲 Randomly selected MCQ question ID: {}", randomQuestion.getId());
            return randomQuestion.getId();
        }
    }

    /**
     * Select next coding question with code quality consideration
     */
    public String selectNextCodingQuestion(PracticeSession session,
                                           List<CodingQuestionDTO> candidateQuestions,
                                           Set<String> usedQuestionIds,
                                           List<Map<String, Object>> codeHistory) {

        // Analyze previous coding patterns if available
        String codeAnalysis = analyzePreviousCodePatterns(codeHistory);

        String performanceContext = buildPerformanceContext(session) +
                "\nCode Analysis: " + codeAnalysis;

        List<CodingQuestionDTO> availableQuestions = candidateQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            log.warn("No new coding questions available for session: {}", session.getId());
            return null;
        }

        String prompt = """
            You are an expert coding coach designing personalized practice sessions.
            
            Student Performance Context:
            %s
            
            Available Coding Questions (%d):
            %s
            
            Based on the student's performance AND coding patterns, select the MOST SUITABLE next question.
            Consider:
            1. Current skill level (adjust difficulty if needed)
            2. Code quality issues from previous submissions
            3. Algorithmic concepts that need practice
            4. Time complexity improvements needed
            5. Edge case handling
            
            Return ONLY the ID of the selected question.
            """.formatted(
                performanceContext,
                availableQuestions.size(),
                buildCodingQuestionsSummary(availableQuestions)
        );

        ChatClient chatClient = ChatClient.create(chatModel);
        String selectedQuestionId = chatClient.prompt()
                .system("You are an expert coding coach. Respond with ONLY the question ID.")
                .user(prompt)
                .call()
                .content()
                .trim();

        log.info("AI selected coding question ID: {} for session: {}", selectedQuestionId, session.getId());
        return selectedQuestionId;
    }

    /**
     * Select next MCQ question with concept reinforcement
     */
    public String selectNextMcqQuestion(PracticeSession session,
                                        List<McqQuestionResponseDTO> candidateQuestions,
                                        Set<String> usedQuestionIds,
                                        List<Map<String, Object>> mcqHistory) {

        // Analyze MCQ performance patterns
        String mcqAnalysis = analyzeMcqPerformancePatterns(mcqHistory);

        String performanceContext = buildPerformanceContext(session) +
                "\nMCQ Analysis: " + mcqAnalysis;

        List<McqQuestionResponseDTO> availableQuestions = candidateQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            log.warn("No new MCQ questions available for session: {}", session.getId());
            return null;
        }

        String prompt = """
            You are an expert MCQ test designer.
            
            Student Performance Context:
            %s
            
            Available MCQ Questions:
            %s
            
            Select the best next question considering:
            1. Knowledge gaps identified from previous answers
            2. Difficulty progression based on accuracy and speed
            3. Topic reinforcement for weak areas
            4. Time spent on previous questions
            5. Common misconception patterns
            
            Return ONLY the question ID.
            """.formatted(
                performanceContext,
                buildMcqQuestionsSummaryWithDetails(availableQuestions)
        );

        ChatClient chatClient = ChatClient.create(chatModel);
        String selectedQuestionId = chatClient.prompt()
                .system("Select the best next MCQ question. Respond with ONLY the question ID.")
                .user(prompt)
                .call()
                .content()
                .trim();

        log.info("AI selected MCQ question ID: {} for session: {}", selectedQuestionId, session.getId());
        return selectedQuestionId;
    }

    /**
     * Adjust difficulty based on performance
     */
    public Difficulty adjustDifficulty(PracticeSession session) {
        log.info("🎯 Adjusting difficulty based on performance...");
        
        double accuracy = (session.getTotalQuestionsAnswered() > 0) 
            ? (session.getCorrectAnswers() * 100.0 / session.getTotalQuestionsAnswered()) 
            : 0;
        
        double avgTime = session.getAverageTimePerQuestion();
        
        // Different thresholds for coding vs MCQ
        double accuracyThreshold = "CODING".equals(session.getQuestionType()) ? 70 : 80;
        double timeThreshold = "CODING".equals(session.getQuestionType()) ? 120 : 60;
        
        Difficulty currentDifficulty = session.getCurrentDifficulty();
        
        // Try AI-powered difficulty adjustment, fallback to rule-based
        try {
            String prompt = """
                Based on student performance in %s questions, suggest next difficulty level.
                Current Difficulty: %s
                Accuracy: %.2f%%
                Average Time: %.2f seconds
                Total Questions: %d
                Correct: %d
                
                Rules for %s:
                - If accuracy > %.0f%% AND avg time < %.0fs → Increase difficulty
                - If accuracy < %.0f%% OR avg time > %.0fs → Decrease difficulty
                - Otherwise → Maintain current difficulty
                
                Return ONLY: EASY, MEDIUM, or HARD
                """.formatted(
                    session.getQuestionType(),
                    currentDifficulty,
                    accuracy,
                    avgTime,
                    session.getTotalQuestionsAnswered(),
                    session.getCorrectAnswers(),
                    session.getQuestionType(),
                    accuracyThreshold,
                    timeThreshold,
                    accuracyThreshold - 20,
                    timeThreshold * 1.5
            );

            ChatClient chatClient = ChatClient.create(chatModel);
            String suggestedDifficulty = chatClient.prompt()
                    .system("Suggest difficulty level. Respond with ONLY: EASY, MEDIUM, or HARD")
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .toUpperCase();

            return Difficulty.valueOf(suggestedDifficulty);
            
        } catch (Exception e) {
            log.warn("⚠️ AI difficulty adjustment failed, using rule-based logic");
            
            // Fallback: Rule-based difficulty adjustment
            if (accuracy > accuracyThreshold && avgTime < timeThreshold) {
                if (currentDifficulty == Difficulty.EASY) return Difficulty.MEDIUM;
                if (currentDifficulty == Difficulty.MEDIUM) return Difficulty.HARD;
            }
            
            if (accuracy < (accuracyThreshold - 20) || avgTime > (timeThreshold * 1.5)) {
                if (currentDifficulty == Difficulty.HARD) return Difficulty.MEDIUM;
                if (currentDifficulty == Difficulty.MEDIUM) return Difficulty.EASY;
            }
            
            return currentDifficulty;
        }
    }

    /**
     * Generate personalized feedback and recommendations
     */
    public RecommendationsDTO generateRecommendations(PracticeSession session) {
        log.info("📊 Generating recommendations...");
        
        // Try AI-powered recommendations, fallback to simple logic
        try {
            String performanceContext = buildPerformanceContext(session);
            
            String prompt = """
                Based on this practice session performance:
                %s
                
                Provide specific recommendations for %s questions.
                Consider:
                1. Common mistakes in %s questions
                2. Time management strategies
                3. Topic-specific improvements
                
                Provide recommendations in this JSON format:
                {
                  "nextTopicsToPractice": ["array of topic names"],
                  "suggestedDifficulty": "EASY/MEDIUM/HARD",
                  "suggestedQuestionCount": number,
                  "questionType": "%s",
                  "specificQuestionIds": ["optional specific question IDs"],
                  "studyPlan": "brief study plan description for %s"
                }
                
                Return ONLY valid JSON.
                """.formatted(
                    session.getQuestionType(),
                    performanceContext,
                    session.getQuestionType(),
                    session.getQuestionType(),
                    session.getQuestionType(),
                    session.getQuestionType()
            );

            ChatClient chatClient = ChatClient.create(chatModel);
            String jsonResponse = chatClient.prompt()
                    .system("You are a learning coach. Respond with ONLY valid JSON.")
                    .user(prompt)
                    .call()
                    .content();

            return parseRecommendations(jsonResponse, session.getQuestionType());
            
        } catch (Exception e) {
            log.warn("⚠️ AI recommendations failed, using simple logic");
            
            // Fallback: Simple rule-based recommendations
            RecommendationsDTO recommendations = new RecommendationsDTO();
            recommendations.setQuestionType(session.getQuestionType());
            
            double accuracy = (session.getTotalQuestionsAnswered() > 0) 
                ? (session.getCorrectAnswers() * 100.0 / session.getTotalQuestionsAnswered()) 
                : 0;
            
            if (accuracy > 80) {
                recommendations.setSuggestedDifficulty(Difficulty.HARD.name());
                recommendations.setNextTopicsToPractice(List.of("Advanced " + session.getQuestionType()));
            } else if (accuracy > 60) {
                recommendations.setSuggestedDifficulty(Difficulty.MEDIUM.name());
                recommendations.setNextTopicsToPractice(List.of("Intermediate " + session.getQuestionType()));
            } else {
                recommendations.setSuggestedDifficulty(Difficulty.EASY.name());
                recommendations.setNextTopicsToPractice(List.of("Basic " + session.getQuestionType()));
            }
            
            recommendations.setSuggestedQuestionCount(5);
            recommendations.setStudyPlan("Continue practicing to improve your skills!");
            
            return recommendations;
        }
    }

    private String buildPerformanceContext(PracticeSession session) {
        return """
            Student ID: %s
            Session Type: %s
            Current Difficulty: %s
            Questions Answered: %d/%d
            Correct Answers: %d
            Accuracy: %.2f%%
            Average Time per Question: %.2f seconds
            Topic: %s
            """.formatted(
                session.getUser().getId(),
                session.getQuestionType(),
                session.getCurrentDifficulty(),
                session.getTotalQuestionsAnswered(),
                session.getMaxQuestions(),
                session.getCorrectAnswers(),
                session.getAccuracyPercentage(),
                session.getAverageTimePerQuestion(),
                session.getTopic() != null ? session.getTopic() : "Mixed"
        );
    }

    private String buildQuestionsSummary(List<CodingQuestionDTO> questions) {
        StringBuilder summary = new StringBuilder();
        for (CodingQuestionDTO q : questions) {
            summary.append(String.format(
                    "- ID: %s | Title: %s | Difficulty: %s | Tags: %s\n",
                    q.getId(), q.getTitle(), q.getDifficulty(), q.getTags()
            ));
        }
        return summary.toString();
    }

    private String buildMcqQuestionsSummary(List<McqQuestionResponseDTO> questions) {
        StringBuilder summary = new StringBuilder();
        for (McqQuestionResponseDTO q : questions) {
            summary.append(String.format(
                    "- ID: %s | Title: %s | Difficulty: %s | Tags: %s\n",
                    q.getId(), q.getTitle(), q.getDifficulty(), q.getTags()
            ));
        }
        return summary.toString();
    }

    private String buildCodingQuestionsSummary(List<CodingQuestionDTO> questions) {
        StringBuilder summary = new StringBuilder();
        for (CodingQuestionDTO q : questions) {
            summary.append(String.format(
                    "- ID: %s | Title: %s | Difficulty: %s | Tags: %s | Time Limit: %.0fs\n",
                    q.getId(), q.getTitle(), q.getDifficulty(), q.getTags(), q.getTimeLimit()
            ));
        }
        return summary.toString();
    }

    private String buildMcqQuestionsSummaryWithDetails(List<McqQuestionResponseDTO> questions) {
        StringBuilder summary = new StringBuilder();
        for (McqQuestionResponseDTO q : questions) {
            summary.append(String.format(
                    "- ID: %s | Title: %s | Difficulty: %s | Tags: %s | Time Limit: %ds | Options: %d\n",
                    q.getId(), q.getTitle(), q.getDifficulty(), q.getTags(),
                    q.getTimeLimit(), q.getOptions().size()
            ));
        }
        return summary.toString();
    }

    /**
     * Analyze previous code submissions for patterns
     */
    private String analyzePreviousCodePatterns(List<Map<String, Object>> codeHistory) {
        if (codeHistory == null || codeHistory.isEmpty()) {
            return "No previous code submissions to analyze.";
        }

        // Extract code snippets from history
        List<String> codeSnippets = codeHistory.stream()
                .filter(entry -> "CODING".equals(entry.get("questionType")))
                .map(entry -> (String) entry.get("codeSnippet"))
                .filter(Objects::nonNull)
                .toList();

        if (codeSnippets.isEmpty()) {
            return "No code snippets available for analysis.";
        }

        // Create analysis prompt for AI
        String codeSamples = String.join("\n---\n", codeSnippets);
        String truncatedCode = truncateSafely(codeSamples, 2000);

        String analysisPrompt = """
            Analyze these code snippets from a student's practice session:
            
            %s
            
            Provide brief analysis of:
            1. Common coding patterns
            2. Potential improvements
            3. Algorithmic understanding
            4. Code readability
            
            Keep analysis concise (2-3 sentences).
            """.formatted(truncatedCode);

        try {
            ChatClient chatClient = ChatClient.create(chatModel);
            return chatClient.prompt()
                    .system("You are a code reviewer. Provide concise analysis.")
                    .user(analysisPrompt)
                    .call()
                    .content()
                    .trim();
        } catch (Exception e) {
            log.error("Error analyzing code patterns: {}", e.getMessage());
            return "Unable to analyze code patterns.";
        }
    }

    /**
     * Analyze MCQ performance patterns
     */
    private String analyzeMcqPerformancePatterns(List<Map<String, Object>> mcqHistory) {
        if (mcqHistory == null || mcqHistory.isEmpty()) {
            return "No MCQ history available for analysis.";
        }

        // Extract MCQ-specific data
        List<Map<String, Object>> mcqEntries = mcqHistory.stream()
                .filter(entry -> "MCQ".equals(entry.get("questionType")))
                .toList();

        if (mcqEntries.isEmpty()) {
            return "No MCQ submissions in history.";
        }

        // Calculate basic metrics
        long totalMcq = mcqEntries.size();
        long correctMcq = mcqEntries.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.get("isCorrect")))
                .count();

        double mcqAccuracy = totalMcq > 0 ? (double) correctMcq / totalMcq * 100 : 0;

        // Analyze time patterns
        double avgTime = mcqEntries.stream()
                .mapToDouble(entry -> ((Number) entry.getOrDefault("timeTaken", 0)).doubleValue())
                .average()
                .orElse(0);

        // Look for patterns in incorrect answers
        List<String> incorrectPatterns = analyzeIncorrectPatterns(mcqEntries);

        return String.format(
                "MCQ Performance: %.1f%% accuracy (%d/%d), Avg time: %.1fs\nPatterns: %s",
                mcqAccuracy, correctMcq, totalMcq, avgTime,
                incorrectPatterns.isEmpty() ? "No clear patterns" : String.join(", ", incorrectPatterns)
        );
    }

    private List<String> analyzeIncorrectPatterns(List<Map<String, Object>> mcqEntries) {
        List<String> patterns = new ArrayList<>();

        // Check for time-related issues
        long rushedAnswers = mcqEntries.stream()
                .filter(entry -> !Boolean.TRUE.equals(entry.get("isCorrect")))
                .filter(entry -> ((Number) entry.getOrDefault("timeTaken", 0)).doubleValue() < 10)
                .count();

        if (rushedAnswers > mcqEntries.size() * 0.3) {
            patterns.add("Rushing through questions");
        }

        // Check for time-out issues
        long slowAnswers = mcqEntries.stream()
                .filter(entry -> !Boolean.TRUE.equals(entry.get("isCorrect")))
                .filter(entry -> ((Number) entry.getOrDefault("timeTaken", 0)).doubleValue() > 60)
                .count();

        if (slowAnswers > mcqEntries.size() * 0.3) {
            patterns.add("Taking too long on difficult questions");
        }

        return patterns;
    }

    /**
     * Parse JSON from AI response
     */
    private RecommendationsDTO parseRecommendations(String json, String questionType) {
        try {
            // Clean JSON response - remove markdown, extra text
            String cleanedJson = cleanJsonResponse(json);

            log.debug("Attempting to parse AI recommendations from: {}", cleanedJson);

            // Parse JSON to DTO
            RecommendationsDTO recommendations = objectMapper.readValue(
                    cleanedJson,
                    RecommendationsDTO.class
            );

            // Validate and set defaults for missing fields
            if (recommendations.getQuestionType() == null || recommendations.getQuestionType().isBlank()) {
                recommendations.setQuestionType(questionType);
            }

            if (recommendations.getNextTopicsToPractice() == null || recommendations.getNextTopicsToPractice().isEmpty()) {
                recommendations.setNextTopicsToPractice(getDefaultTopics(questionType));
            }

            if (recommendations.getSuggestedDifficulty() == null) {
                recommendations.setSuggestedDifficulty("MEDIUM");
            }

            if (recommendations.getSuggestedQuestionCount() <= 0) {
                recommendations.setSuggestedQuestionCount(questionType.equals("CODING") ? 8 : 12);
            }

            if (recommendations.getStudyPlan() == null || recommendations.getStudyPlan().isBlank()) {
                recommendations.setStudyPlan(getDefaultStudyPlan(questionType));
            }

            log.info("Successfully parsed AI recommendations for question type: {}", questionType);
            return recommendations;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI recommendations JSON: {}", e.getMessage());
            log.debug("Problematic JSON response: {}", json);
            return getDefaultRecommendations(questionType);

        } catch (Exception e) {
            log.error("Unexpected error parsing AI recommendations: {}", e.getMessage(), e);
            return getDefaultRecommendations(questionType);
        }
    }

    /**
     * Clean JSON response by removing markdown and extracting JSON object
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            log.warn("Received null or empty JSON response");
            return "{}";
        }

        // Remove markdown code blocks
        String cleaned = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        // Remove any leading text before JSON starts
        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }

        // Remove any trailing text after JSON ends
        if (cleaned.endsWith("}")) {
            return cleaned;
        }

        log.warn("Could not extract valid JSON from response, using empty object");
        return "{}";
    }

    /**
     * Get default topics based on question type
     */
    private List<String> getDefaultTopics(String questionType) {
        if ("CODING".equals(questionType)) {
            return List.of("Arrays", "Strings", "Dynamic Programming", "Graphs");
        } else {
            return List.of("Core Concepts", "Problem Solving", "Time Management");
        }
    }

    /**
     * Get default study plan based on question type
     */
    private String getDefaultStudyPlan(String questionType) {
        if ("CODING".equals(questionType)) {
            return "Focus on:\n" +
                    "1. Understanding time and space complexity\n" +
                    "2. Practicing edge case handling\n" +
                    "3. Writing clean, readable code\n" +
                    "4. Implementing efficient algorithms";
        } else {
            return "Focus on:\n" +
                    "1. Understanding fundamental concepts thoroughly\n" +
                    "2. Practicing time management for quick recall\n" +
                    "3. Identifying common misconceptions\n" +
                    "4. Building confidence in knowledge areas";
        }
    }

    private RecommendationsDTO getDefaultRecommendations(String questionType) {
        return RecommendationsDTO.builder()
                .nextTopicsToPractice(getDefaultTopics(questionType))
                .suggestedDifficulty("MEDIUM")
                .suggestedQuestionCount(questionType.equals("CODING") ? 8 : 12)
                .questionType(questionType)
                .specificQuestionIds(List.of())
                .studyPlan(getDefaultStudyPlan(questionType))
                .build();
    }

    /**
     * Safe string truncation that won't break JSON
     */
    private String truncateSafely(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;

        // Truncate and remove any incomplete escape sequences
        String truncated = text.substring(0, maxLength);

        // Remove trailing backslash (incomplete escape)
        if (truncated.endsWith("\\")) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }

        // Remove any incomplete quotes
        long quoteCount = truncated.chars().filter(ch -> ch == '"').count();
        if (quoteCount % 2 != 0) {
            int lastQuote = truncated.lastIndexOf('"');
            if (lastQuote > 0) {
                truncated = truncated.substring(0, lastQuote);
            }
        }

        return truncated + "... [truncated]";
    }
}