package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * A unified DTO structure to carry either a CodingQuestion or an McqQuestion
 * back to the client after a match starts.
 */
@Data
public class QuestionFetchDTO {
    // Common fields
    private String id; // This is the ID of the CODING or MCQ question
    private String type; // "Coding Question" or "MCQ Question"
    private String title;
    private String description;
    private Difficulty difficulty;
    private int points;
    private int timeLimit; // in seconds/minutes

    // Coding specific fields
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private List<String> tags;
    private List<StarterCodeDTO> starterCodes;
    // NOTE: We generally avoid sending ALL non-sample test cases to the client,
    // so we reuse TestCaseJsonDTO here, assuming it contains sample test cases only.
    private List<TestCaseJsonDTO> sampleTestCases;


    // MCQ specific fields (McqOptionJsonDTO is already suitable as it includes 'isCorrect')
    private List<McqOptionJsonDTO> options;

}