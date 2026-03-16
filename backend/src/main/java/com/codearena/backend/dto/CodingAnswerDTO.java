package com.codearena.backend.dto;

import lombok.Data;

@Data
public class CodingAnswerDTO {

    private String questionId;

    private String language;

    private String sourceCode;
}

