package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifficultyPerformance {
    private String difficulty;
    private int totalQuestions;
    private int correctAnswers;
    private double accuracy;
    private long totalTimeSeconds;
    private double averageTime;
}