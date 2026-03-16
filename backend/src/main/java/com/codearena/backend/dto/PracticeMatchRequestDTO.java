package com.codearena.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeMatchRequestDTO {

    @NotBlank(message = "Question type is required")
    private String questionType; // "CODING" or "MCQ"

    @Min(value = 1, message = "At least 1 question is required")
    private int maxQuestions;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private int timeLimitMinutes;

    private String topic; // Optional: specific topic like "Arrays", "DP", "OOP"

    private String difficultyPreference; // Optional: "EASY", "MEDIUM", "HARD", "ADAPTIVE"
}
