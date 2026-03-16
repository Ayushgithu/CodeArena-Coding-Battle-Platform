package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationsDTO {
    private List<String> nextTopicsToPractice;
    private String suggestedDifficulty;
    private int suggestedQuestionCount;
    private String questionType;  // Add this field
    private List<String> specificQuestionIds;
    private String studyPlan;
}