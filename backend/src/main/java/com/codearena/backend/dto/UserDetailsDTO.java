package com.codearena.backend.dto;

import lombok.Data;

@Data
public class UserDetailsDTO {
    private String id;
    private String userId;
    private String username;
    private String name;
    private int userRank;
    private String email;
    private int totalWin;
    private int totalLoss;
    private int totalBattle;
    private int highestStreak;
}
