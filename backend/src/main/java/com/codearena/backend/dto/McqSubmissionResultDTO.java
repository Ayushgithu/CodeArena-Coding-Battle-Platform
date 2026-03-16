package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McqSubmissionResultDTO {
    private String questionId;
    private String selectedOptionId;
    private boolean isCorrect;
    private List<String> correctOptionIds; // For multiple correct answers
    private String explanation;
    private long timeTakenSeconds;
    private double timeConfidenceScore;
    private String difficultyLevel;
    private List<String> tags;
    private int pointsEarned;
    private LocalDateTime submittedAt;
}