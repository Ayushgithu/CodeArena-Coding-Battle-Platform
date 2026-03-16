package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultResponseDTO {

    private String userId;
//   private String username;
    private int score;
    private long totalTime;
    private boolean winner;
}