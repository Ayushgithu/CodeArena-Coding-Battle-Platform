package com.codearena.backend.serviceImpl;

import com.codearena.backend.config.AppProperties;
import com.codearena.backend.dto.AIReviewRequest;
import com.codearena.backend.dto.AIReviewResponse;
import com.codearena.backend.service.AIService;
import com.codearena.backend.utils.constant.ErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    // Thread pool for potential future async tasks or timeouts
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public AIReviewResponse getAIReview(AIReviewRequest request) {
        // 1. Validate request
        validateRequest(request);

        // 2. Prepare Prompt
        String prompt = String.format("""
            You are a professional code reviewer.

            Analyze the following and return ONLY valid JSON in this exact format:
            {
              "summary": "...",
              "optimizationTips": "...",
              "readabilityScore": 1-10
            }

            Problem: %s
            Language: %s

            Code:
            ---
            %s
            ---

            Provide a review response strictly in JSON format with three fields:
            1. summary: A concise summary of the code's function.
            2. optimizationTips: Specific, actionable tips for improving performance.
            3. readabilityScore: An integer between 1 and 10.

            Return ONLY the JSON object. No markdown, no "Here is the JSON", no backticks.
            """,
                request.getProblemStatement(),
                request.getLanguage(),
                request.getSourceCode()
        );

        try {
            // 3. Create Client and Call AI
            ChatClient chatClient = ChatClient.builder(chatModel).build();

            String rawResponse = chatClient.prompt()
                    .system("You are a strict JSON API. Return ONLY valid JSON. No markdown. No explanations.")
                    .user(prompt)
                    .call()
                    .content();

            // 4. Parse and Return
            return parseAIResponse(rawResponse);

        } catch (Exception e) {
            log.error("AI Service communication error: {}", e.getMessage(), e);
            return createDefaultResponse("The AI service is currently unavailable. Please try again later.");
        }
    }

    /**
     * Parse AI response with robust error handling
     */
    private AIReviewResponse parseAIResponse(String jsonResponse) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanedJson = cleanJsonResponse(jsonResponse);

            // Parse JSON
            AIReviewResponse response = objectMapper.readValue(cleanedJson, AIReviewResponse.class);

            // Validate fields exist
            if (response.getSummary() == null || response.getSummary().isBlank()) {
                log.warn("AI response missing summary, using fallback");
                return createDefaultResponse("AI provided incomplete response.");
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            log.debug("Problematic JSON: {}", jsonResponse);
            return createDefaultResponse("AI response format was invalid.");
        }
    }

    /**
     * Clean JSON response - remove markdown (```json) and extra text
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }

        // Remove markdown code blocks and whitespace
        String cleaned = response.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // Find JSON object boundaries to ignore preamble/postscript text
        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }

        return cleaned;
    }

    /**
     * Create default response for failures
     */
    private AIReviewResponse createDefaultResponse(String errorDetail) {
        return new AIReviewResponse(
                "Unable to generate detailed review. " + errorDetail,
                "General Tips: Ensure your code handles edge cases, use meaningful variable names, and check time complexity.",
                "N/A"
        );
    }

    /**
     * Validate AI review request
     */
    private void validateRequest(AIReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI review request cannot be null");
        }
        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            throw new IllegalArgumentException("Source code is required for AI review");
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Programming language is required");
        }

        // Check code length based on properties
        int maxCodeLength = appProperties.getSubmission().getMaxCodeLength();
        if (request.getSourceCode().length() > maxCodeLength) {
            throw new IllegalArgumentException(
                    ErrorMessages.format(ErrorMessages.CODE_TOO_LONG, maxCodeLength)
            );
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}














