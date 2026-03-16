package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionStatusDTO {

    private String questionId;
    private boolean solved;
    private int attempts;
}

