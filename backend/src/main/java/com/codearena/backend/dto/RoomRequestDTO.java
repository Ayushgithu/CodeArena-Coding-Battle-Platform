package com.codearena.backend.dto;

import lombok.Data;

@Data
public class RoomRequestDTO {
    private String questionType;
    private int noOfQuestions;
    private String difficulty;
    private int duration;
}
