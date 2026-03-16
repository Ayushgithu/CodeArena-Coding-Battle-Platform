package com.codearena.backend.service.ai;

import com.codearena.backend.dto.CodingQuestionDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;
import com.codearena.backend.dto.RecommendationsDTO;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.utils.constant.Difficulty;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdaptiveQuestionSelector {
    String selectNextCodingQuestion(PracticeSession session, List<CodingQuestionDTO> candidateQuestions,
                                    Set<String> usedQuestionIds);
    String selectNextMcqQuestion(PracticeSession session, List<McqQuestionResponseDTO> candidateQuestions,
                                 Set<String> usedQuestionIds);
    String selectNextCodingQuestion(PracticeSession session, List<CodingQuestionDTO> candidateQuestions,
                                    Set<String> usedQuestionIds, List<Map<String, Object>> codeHistory);
    String selectNextMcqQuestion(PracticeSession session, List<McqQuestionResponseDTO> candidateQuestions,
                                 Set<String> usedQuestionIds, List<Map<String, Object>> mcqHistory);
    Difficulty adjustDifficulty(PracticeSession session);
    RecommendationsDTO generateRecommendations(PracticeSession session);
}