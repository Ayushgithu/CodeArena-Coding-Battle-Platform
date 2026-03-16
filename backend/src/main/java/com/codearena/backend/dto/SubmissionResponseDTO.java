package com.codearena.backend.dto;

import lombok.Data;

/**
 * DTO to return the result of a single submission (before final match evaluation).
 */
@Data
public class SubmissionResponseDTO {
    private String userId;
    private String status; // e.g., "PENDING", "ACCEPTED", "WRONG_ANSWER", "MCQ_CORRECT"
    private int score; // Score awarded for this specific submission
    private double executionTime; // Total time taken (relevant for tie-breaking)
    private String message; // Human-readable feedback
    private String submissionId; // ID of the submission
    private String questionId; // ID of the question (coding )
}