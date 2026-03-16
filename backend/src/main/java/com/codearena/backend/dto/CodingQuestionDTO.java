package com.codearena.backend.dto;


import com.codearena.backend.utils.constant.Difficulty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CodingQuestionDTO {
    private String id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private List<String> tags;
    private int points;
    private double timeLimit; // in seconds
    private double memoryLimit;
    //private String starterCode;
    private List<TestCaseJsonDTO> testCases;
    private Date createdAt;
    private String createdBy;
    private List<StarterCodeDTO> starterCodes;



}
