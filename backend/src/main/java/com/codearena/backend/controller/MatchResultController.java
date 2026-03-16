package com.codearena.backend.controller;

import com.codearena.backend.dto.MatchResultResponseDTO;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.service.MatchResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match-results")
@RequiredArgsConstructor
public class MatchResultController {

    private final MatchResultService matchResultService;
    private final RoomRepository roomRepository;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<MatchResultResponseDTO>>
    getRoomResults(@PathVariable String roomId) {

        return ResponseEntity.ok(
                matchResultService.getRoomResults(roomId)
        );
    }
}
