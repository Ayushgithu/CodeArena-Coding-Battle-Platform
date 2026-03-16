package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultDTO {
    private String questionId;
    private String questionType;
    private boolean isCorrect;
    private long timeTakenSeconds;
    private int sessionProgress;
    private int totalQuestions;
    private int correctAnswers;
    private double accuracyPercentage;
    private String message;
    private boolean nextQuestionAvailable;

    // For coding questions
    private Integer testCasesPassed;
    private Integer totalTestCases;
    private String compilationError;

    // For MCQ questions
    private String correctOptionId;
    private String explanation;
}