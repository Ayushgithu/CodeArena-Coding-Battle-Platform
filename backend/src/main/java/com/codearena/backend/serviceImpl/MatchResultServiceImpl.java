package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.MatchResultResponseDTO;
import com.codearena.backend.entity.MatchResult;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.repository.MatchResultRepository;
import com.codearena.backend.service.MatchResultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Transactional
public class MatchResultServiceImpl implements MatchResultService {
    private final MatchResultRepository matchResultRepository;

    @Override
    public void saveMatchResults(Room room,
                                 Map<User, Integer> scores,
                                 Map<User, Long> times) {

        // Determine winner (highest score, lowest time)

        User winner = scores.entrySet()
                .stream()
                .sorted((a, b) -> {

                    int scoreCompare = b.getValue()
                            .compareTo(a.getValue());

                    if (scoreCompare != 0) return scoreCompare;

                    return times.get(a.getKey())
                            .compareTo(times.get(b.getKey()));
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        for (User user : scores.keySet()) {

            MatchResult result = new MatchResult();

            result.setRoom(room);
            result.setUser(user);
            result.setScore(scores.get(user));
            result.setTotalTime(times.get(user));
            result.setWinner(user.equals(winner));

            matchResultRepository.save(result);
        }
    }

    @Override
    public List<MatchResultResponseDTO> getRoomResults(String roomId) {

        List<MatchResult> results =
                matchResultRepository.findByRoomId(roomId);

        return results.stream()
                .map(r -> new MatchResultResponseDTO(
                        r.getUser().getId(),
                        r.getScore(),
                        r.getTotalTime(),
                        r.isWinner()
                ))
                .toList();
    }
}
