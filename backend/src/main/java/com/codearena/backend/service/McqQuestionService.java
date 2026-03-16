package com.codearena.backend.service;

import com.codearena.backend.dto.McqQuestionJsonDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;

import java.util.List;

public interface McqQuestionService {
    McqQuestionResponseDTO create(McqQuestionJsonDTO dto);
    McqQuestionResponseDTO update(String id, McqQuestionJsonDTO dto);
    void delete(String id);
    McqQuestionResponseDTO getById(String id);
    List<McqQuestionResponseDTO> getAll();
    List<McqQuestionResponseDTO> getByDifficultyAndCount(String difficulty, int count);
    List<McqQuestionResponseDTO> getByDifficultyAndTopicAndCount(String difficulty, String topic, int count);
    List<McqQuestionResponseDTO> getMixedDifficultyQuestions(int count);


}
