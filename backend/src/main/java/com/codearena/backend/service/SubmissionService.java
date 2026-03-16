package com.codearena.backend.service;

import com.codearena.backend.dto.*;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface SubmissionService {
    /**
     * Handles a unified submission request for either a coding question or an MCQ.
     * Processes the submission, evaluates correctness, updates DB, and checks for match completion.
     *
     * @param submissionRequestDTO The DTO containing the submission data.
     * @param principal The security principal of the submitting user.
     * @return Details about the submission result.
     */
    // Unified endpoint for single question submission

    // Bulk submission for room (MCQ/Coding)
    RoomResultResponseDTO submitRoomAnswers(SubmissionRequestDTO submissionRequestDTO, Principal principal);


    List<SubmissionResponseDTO> getAllSubmissionByRoomCodeForCurrentUser(int roomCode);

//    CodeExecutionResultDTO submitCodingRoomAnswers(String codingQuestionId, int roomCode, CodeExecutionDTO codeExecutionDTO);
}