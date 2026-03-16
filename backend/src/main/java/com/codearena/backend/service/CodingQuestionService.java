package com.codearena.backend.service;

import com.codearena.backend.dto.CodingQuestionDTO;
import java.util.List;

public interface CodingQuestionService {
    CodingQuestionDTO createQuestion(CodingQuestionDTO dto);

    List<CodingQuestionDTO> getAllQuestions();

    CodingQuestionDTO getQuestionById(String id);

    CodingQuestionDTO updateQuestion(String id, CodingQuestionDTO dto);

    void deleteQuestion(String id);
    List<CodingQuestionDTO> getByDifficultyAndCount(String difficulty, int count);
    List<CodingQuestionDTO> getByDifficultyAndTopicAndCount(String difficulty, String topic, int count);
    List<CodingQuestionDTO> getMixedQuestions(int count);




}
