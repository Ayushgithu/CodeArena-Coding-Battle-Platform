package com.codearena.backend.controller;

import com.codearena.backend.dto.StandardResponse;
import com.codearena.backend.exception.AiServiceException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Slf4j
public class AiController {

    private final ChatClient chatClient;

    // Manual constructor to build ChatClient from ChatModel (Groq)
    public AiController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @PostMapping("/chat")
    public ResponseEntity<StandardResponse<Map<String, String>>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(StandardResponse.error("Message is required"));
            }

            log.info("AI chat request received: {}", message.substring(0, Math.min(50, message.length())));

            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            return ResponseEntity.ok(
                    StandardResponse.success("AI response generated", Map.of("response", response))
            );

        } catch (Exception e) {
            log.error("AI Service Error: {}", e.getMessage());
            // This will trigger the GlobalExceptionHandler
            throw new AiServiceException("AI is currently busy or quota exceeded. Please try again later.");
        }
    }

    @PostMapping("/hint")
    public ResponseEntity<StandardResponse<Map<String, String>>> getHint(@RequestBody Map<String, String> request) {
        try {
            String questionTitle = request.get("questionTitle");
            String questionDescription = request.get("questionDescription");

            if (questionTitle == null || questionDescription == null) {
                return ResponseEntity.badRequest()
                        .body(StandardResponse.error("Question title and description are required"));
            }

            String prompt = String.format(
                    "Give a helpful hint for solving this coding problem. Don't give the full solution, just a hint to guide the user:\n\nTitle: %s\n\nDescription: %s\n\nProvide a concise hint (2-3 sentences max).",
                    questionTitle, questionDescription
            );

            log.info("Generating hint for question: {}", questionTitle);

            String hint = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return ResponseEntity.ok(
                    StandardResponse.success("Hint generated", Map.of("hint", hint))
            );

        } catch (Exception e) {
            log.error("AI Service Error (Hint): {}", e.getMessage());
            // This will trigger the GlobalExceptionHandler
            throw new AiServiceException("Failed to generate hint. Please try again later.");
        }
    }
}



















