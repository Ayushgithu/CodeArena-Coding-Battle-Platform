package com.codearena.backend.dto;


import lombok.AllArgsConstructor; // <-- ADD THIS
import lombok.Data;
import lombok.NoArgsConstructor; // <-- ADD THIS

@Data
@NoArgsConstructor // <-- ADD THIS
@AllArgsConstructor // <-- ADD THIS
public class AIReviewResponse {
    private String summary;
    private String optimizationTips;
    private String readabilityScore;
}