package com.codearena.backend.dto;

import lombok.Data;

@Data
public class CodeExecutionDTO {
    private String language;
    private String version;
    private String code;
    private String codingQuestionId;
}
