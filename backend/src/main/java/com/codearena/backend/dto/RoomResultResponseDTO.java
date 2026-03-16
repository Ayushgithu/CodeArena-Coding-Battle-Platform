package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomResultResponseDTO {

    private String roomCode;

    private int totalQuestions;

    private int correctAnswers;

    private int score;

    private long timeTaken;

    private String winner;
}

