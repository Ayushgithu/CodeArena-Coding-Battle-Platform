package com.codearena.backend.service;

import com.codearena.backend.dto.CodingQuestionDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;
import com.codearena.backend.dto.PracticeQuestionResponseDTO;
import com.codearena.backend.entity.PracticeSession;
import com.codearena.backend.utils.constant.Difficulty;
import java.util.List;

public interface QuestionFetcherService {
    PracticeQuestionResponseDTO fetchQuestion(String questionId, String questionType, PracticeSession session);
    List<CodingQuestionDTO> getCodingQuestionsByDifficulty(Difficulty difficulty, String topic, int count);
    List<McqQuestionResponseDTO> getMcqQuestionsByDifficulty(Difficulty difficulty, String topic, int count);
    List<McqQuestionResponseDTO> getAllMcqQuestions(String topic, int count); // For adaptive selection
}