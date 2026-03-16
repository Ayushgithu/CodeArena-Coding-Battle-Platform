package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuestionResponseDTO {
    private String sessionId;
    private String questionId;
    private String questionType; // "CODING" or "MCQ"
    private String title;
    private String description;
    private Difficulty difficulty;
    private int questionNumber;
    private int totalQuestions;
    private long timeRemainingSeconds;
    private boolean isLastQuestion;

    // Coding question specific fields
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private List<StarterCodeDTO> starterCodes;
    private List<TestCaseJsonDTO> sampleTestCases; // Only sample test cases

    // MCQ question specific fields
    private List<McqOptionJsonDTO> options; // Options with isCorrect hidden

    // Common metadata
    private List<String> tags;
    private int points;
    private int timeLimit; // in seconds for this specific question
}