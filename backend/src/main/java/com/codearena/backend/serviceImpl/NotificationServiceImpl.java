package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.PracticeResultDTO;
import com.codearena.backend.entity.User;
import com.codearena.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of notification service using WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendPracticeSessionStarted(User user, String sessionId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "PRACTICE_SESSION_STARTED");
        notification.put("sessionId", sessionId);
        notification.put("message", "Practice session started successfully");
        notification.put("timestamp", System.currentTimeMillis());

        sendToUser(user.getUsername(), "/queue/notifications", notification);
        log.info("Sent practice session started notification to user: {}", user.getUsername());
    }

    @Override
    public void sendPracticeSessionCompleted(User user, String sessionId, PracticeResultDTO result) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "PRACTICE_SESSION_COMPLETED");
        notification.put("sessionId", sessionId);
        notification.put("message", "Practice session completed");

        if (result != null) {
            notification.put("accuracy", result.getAccuracyPercentage());
            notification.put("correctAnswers", result.getCorrectAnswers());
            notification.put("totalQuestions", result.getTotalQuestions());
        }

        notification.put("timestamp", System.currentTimeMillis());

        sendToUser(user.getUsername(), "/queue/notifications", notification);
        log.info("Sent practice session completed notification to user: {}", user.getUsername());
    }

    @Override
    public void sendSessionExtended(User user, String sessionId, int additionalMinutes) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "SESSION_EXTENDED");
        notification.put("sessionId", sessionId);
        notification.put("message", "Session extended by " + additionalMinutes + " minutes");
        notification.put("additionalMinutes", additionalMinutes);
        notification.put("timestamp", System.currentTimeMillis());

        sendToUser(user.getUsername(), "/queue/notifications", notification);
        log.info("Sent session extended notification to user: {}", user.getUsername());
    }

    @Override
    public void sendSessionAbandoned(User user, String sessionId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "SESSION_ABANDONED");
        notification.put("sessionId", sessionId);
        notification.put("message", "Practice session abandoned");
        notification.put("timestamp", System.currentTimeMillis());

        sendToUser(user.getUsername(), "/queue/notifications", notification);
        log.info("Sent session abandoned notification to user: {}", user.getUsername());
    }

    /**
     * Helper method to send notification to specific user
     */
    private void sendToUser(String username, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(username, destination, payload);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", username, e.getMessage());
        }
    }
}