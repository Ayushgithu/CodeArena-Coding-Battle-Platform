package com.codearena.backend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class McqOptionJsonDTO {
    private String id;
    private String optionText;
    @JsonProperty("isCorrect")
    private boolean correct;


}