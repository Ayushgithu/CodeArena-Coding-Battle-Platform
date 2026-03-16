package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeHistoryDTO {
    private String sessionId;
    private String sessionCode;
    private String questionType;
    private String topic;
    private Difficulty difficulty;
    private int totalQuestions;
    private int questionsAnswered;
    private int correctAnswers;
    private double accuracyPercentage;
    private long totalTimeTakenSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}