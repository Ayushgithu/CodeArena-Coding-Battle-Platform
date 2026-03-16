package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class McqQuestionResponseDTO {
    private String id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private int points;
    private int timeLimit;
    private List<McqOptionJsonDTO> options;
    private List<String> tags;
    private Date createdAt;
    private String createdBy;
}
