package com.codearena.backend.service;


import com.codearena.backend.dto.MatchResultResponseDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;

import java.util.List;
import java.util.Map;

public interface MatchResultService {

    void saveMatchResults(Room room,
                          Map<User, Integer> scores,
                          Map<User, Long> times);

    List<MatchResultResponseDTO> getRoomResults(String roomId);
}
