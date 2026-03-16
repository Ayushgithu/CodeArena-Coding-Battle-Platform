package com.codearena.backend.dto;

import lombok.Data;

@Data
public class AIReviewRequest {
    private String sourceCode;
    private String language;
    private String problemStatement;
}

