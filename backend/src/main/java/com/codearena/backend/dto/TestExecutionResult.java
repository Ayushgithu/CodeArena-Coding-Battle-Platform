package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionResult {
    private String testCaseId;
    private boolean passed;
    private boolean skipped;
    private String actualOutput;
    private String expectedOutput;
    private String errorMessage;
    private String errorType; // COMPILATION_ERROR, RUNTIME_ERROR, WRONG_ANSWER, TIME_LIMIT_EXCEEDED
    private double executionTime; // in seconds
    private double memoryUsed; // in MB
    private String reason;
}