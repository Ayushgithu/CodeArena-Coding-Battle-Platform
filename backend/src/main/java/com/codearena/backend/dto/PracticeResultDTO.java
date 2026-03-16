package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeResultDTO {
    private String sessionId;
    private String userId;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double accuracyPercentage;
    private long totalTimeTakenSeconds;
    private double averageTimePerQuestion;
    private String overallDifficultyLevel;
    private List<String> strengths; // Topics user is good at
    private List<String> weaknesses; // Topics needing improvement
    private Map<String, Double> topicWisePerformance;
    private String aiFeedback;
    private RecommendationsDTO recommendations;
    private String userName;
    private String sessionCode;
    private String questionType;
    private Map<String, String> performanceMetrics;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}