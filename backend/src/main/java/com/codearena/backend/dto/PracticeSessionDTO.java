package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSessionDTO {
    private String sessionId;
    private String sessionCode;
    private String userId;
    private String questionType;
    private String topic;
    private int maxQuestions;
    private int timeLimitMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private int currentQuestionNumber;
    private int totalQuestionsAnswered;
    private int correctAnswers;
    private double averageTimePerQuestion;
    private Difficulty currentDifficulty;
    private String currentQuestionId;
    private Map<String, String> performanceMetrics;
    private boolean isCompleted;
    private double accuracyPercentage;
    /**
     * Remaining time in minutes before session expires
     */
    private long remainingTimeMinutes;
    private boolean isExpired;
}