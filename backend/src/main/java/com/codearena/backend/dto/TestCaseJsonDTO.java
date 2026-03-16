package com.codearena.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestCaseJsonDTO {

    private String id;
    private String inputData;
    private String expectedOutput;
    @JsonProperty("isSample")
    private boolean sample;
    private int orderIndex;
    private String explanation;
}
