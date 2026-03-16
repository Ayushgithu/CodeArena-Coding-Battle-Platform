package com.codearena.backend.dto;

import lombok.Data;

@Data
public class TestCaseExecutionResultDTO {

    private int orderIndex;

    private String input;

    private String expectedOutput;

    private String actualOutput;

    private boolean passed;

    private String errorMessage;
}

