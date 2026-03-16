package com.codearena.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequestDTO {

    private int roomCode;

    private String questionType; // CODING or MCQ

    // For MCQ Rooms
    private List<McqAnswerDTO> mcqAnswers;

    // For Coding Rooms
    private List<CodingAnswerDTO> codingAnswers;
}
