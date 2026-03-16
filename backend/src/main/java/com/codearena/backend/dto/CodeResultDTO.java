package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeResultDTO {
    private String stdout;
    private String stderr;
    private int exitCode;
}

