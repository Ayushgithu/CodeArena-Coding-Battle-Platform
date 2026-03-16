package com.codearena.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class CodeExecutionResultDTO {
    // Standard Output from the code execution
    private String id;
    private String stdout;
    // Standard Error from the code execution (e.g., runtime errors)
    private String stderr;
    // Exit code from the execution (0 for success)
    private int exitCode;
    // Execution time from the Piston API (in seconds or milliseconds)
    private double time;
    // Memory used from the Piston API (in kilobytes or megabytes)
    private double memory;
    // Any compilation errors
    private String compileOutput;
    private List<TestCaseExecutionResultDTO> testCaseResults;

    private int totalTestCases;
    private int passedTestCases;

}