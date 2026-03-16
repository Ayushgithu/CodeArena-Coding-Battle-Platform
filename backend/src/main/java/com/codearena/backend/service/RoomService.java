package com.codearena.backend.service;

import com.codearena.backend.dto.QuestionFetchDTO;
import com.codearena.backend.dto.RoomRequestDTO;
import com.codearena.backend.dto.RoomResponseDTO;
import com.codearena.backend.entity.Room;

public interface RoomService {
    RoomResponseDTO createRoom(RoomRequestDTO roomRequestDTO);

    RoomResponseDTO joinRoom(int roomCode);

    // ✅ UPDATED: Returns the assigned question details for the joined players
    void startRoom(int roomCode);

    RoomResponseDTO getRoomDetails(int roomCode);

    void endTest(int roomCode);
}