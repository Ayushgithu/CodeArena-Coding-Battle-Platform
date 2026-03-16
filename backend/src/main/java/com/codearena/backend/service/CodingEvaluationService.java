package com.codearena.backend.service;

import com.codearena.backend.dto.CodeExecutionResultDTO;
import com.codearena.backend.dto.CodingEvaluationResult;

public interface CodingEvaluationService {
    CodingEvaluationResult evaluateSubmission(String questionId, String language, String sourceCode, int timeLimitSeconds);
//    boolean checkCompilation(String language, String sourceCode);
//    CodeExecutionResultDTO runCustomInput(String language, String sourceCode, String customInput);
}