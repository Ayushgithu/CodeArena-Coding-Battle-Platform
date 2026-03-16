package com.codearena.backend.service;

import com.codearena.backend.dto.CodeExecutionDTO;
import com.codearena.backend.dto.CodeExecutionResultDTO;

public interface CodeExecutionService {
//    CodeExecutionResultDTO executeCode(CodeExecutionDTO request);
//
//    CodeExecutionResultDTO executeCode1(CodeExecutionDTO request);

    CodeExecutionResultDTO runCode(CodeExecutionDTO request);


    CodeExecutionResultDTO submitCode(CodeExecutionDTO request,int roomCode);
    CodeExecutionResultDTO submitCode(CodeExecutionDTO request);
}
