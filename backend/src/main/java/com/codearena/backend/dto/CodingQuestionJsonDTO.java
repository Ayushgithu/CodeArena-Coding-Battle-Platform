package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.Data;

import java.util.List;

@Data
public class CodingQuestionJsonDTO {
    private String id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private int points;
    private double timeLimit;
    private double memoryLimit;
//    private String starterCode;
    private List<String> tags;
    private List<TestCaseJsonDTO> testCases;

    private List<StarterCodeJsonDTO> starterCodes;
}

