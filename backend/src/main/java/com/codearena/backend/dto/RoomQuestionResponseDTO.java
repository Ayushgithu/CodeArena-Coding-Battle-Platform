package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomQuestionResponseDTO {

    private String questionId;
    private String title;
    private int order;
    private String type;

    private List<McqOptionJsonDTO> options;
}
