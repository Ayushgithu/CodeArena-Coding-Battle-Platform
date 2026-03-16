package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSubmissionDTO {
    private String sessionId;
    private String questionId;
    private String questionType; // "CODING" or "MCQ"

    // For coding submissions
    private String language;
    private String sourceCode;

    // For MCQ submissions
    private String selectedOptionId;

    // For both types - evaluation results from frontend
    private boolean isCorrect;
    private long timeTakenSeconds;

    // Additional metrics for AI analysis
    private double confidenceScore; // User's self-assessment 0-1
    private int attemptsCount; // How many attempts before correct
    private String feedback; // Any additional user feedback

    // For coding questions - execution results
    private CodeExecutionResultDTO executionResult;
    private int testCasesPassed;
    private int totalTestCases;
}