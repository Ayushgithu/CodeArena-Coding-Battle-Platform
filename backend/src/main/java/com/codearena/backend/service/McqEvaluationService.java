package com.codearena.backend.service;

import com.codearena.backend.dto.McqPerformanceAnalysis;
import com.codearena.backend.dto.McqSubmissionResultDTO;
import com.codearena.backend.entity.PracticeSession;
import java.util.List;
import java.util.Map;

public interface McqEvaluationService {
    McqSubmissionResultDTO evaluateMcqSubmission(String questionId, String selectedOptionId, long timeTakenSeconds);
    List<McqSubmissionResultDTO> evaluateBatchMcqSubmissions(Map<String, String> questionIdToSelectedOption,
                                                             Map<String, Long> questionIdToTimeTaken);
    McqPerformanceAnalysis analyzeMcqPerformance(PracticeSession session, List<McqSubmissionResultDTO> submissionHistory);
    String generatePersonalizedExplanation(String questionId, String selectedOptionId, boolean isCorrect,
                                           List<McqSubmissionResultDTO> previousSubmissions);
    String recommendNextMcqQuestion(PracticeSession session, List<McqSubmissionResultDTO> submissionHistory,
                                    List<String> availableQuestionIds);
}