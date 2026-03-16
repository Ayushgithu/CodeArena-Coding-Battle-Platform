package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingEvaluationResult {
    private boolean passed;
    private String message;
    private int totalTestCases;
    private int passedTestCases;
    private List<TestExecutionResult> executionResults;
    private double averageExecutionTime;
    private double maxMemoryUsed;
    private String compilationError;
    private String suggestedImprovements;
}