package com.codearena.backend.service;

import com.codearena.backend.dto.CodingQuestionJsonDTO;
import com.codearena.backend.dto.QuestionStatusDTO;
import com.codearena.backend.dto.RoomQuestionResponseDTO;

import java.util.List;

public interface RoomQuestionService {
    void assignQuestionsToRoom(int roomCode);
    List<RoomQuestionResponseDTO> getRoomQuestions(int roomCode);
//    void markQuestionAsSolved(String roomQuestionId);
//    int getSolvedQuestionCount(int roomCode);
//    int getTotalQuestionCount(int roomCode);
//    RoomQuestionResponseDTO getNextUnsolvedQuestion(int roomCode, int currentOrder);
//    void resetRoomQuestions(int roomCode);
    List<CodingQuestionJsonDTO> getRoomCodingQuestions(int roomCode);
    List<QuestionStatusDTO> getRoomQuestionStatus(int roomCode);

}

