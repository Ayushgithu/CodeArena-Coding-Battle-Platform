package com.codearena.backend.repository;

import com.codearena.backend.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult,String> {
    //  Check if match results already exist for a room (to prevent re-evaluation)
    boolean existsByRoomId(String roomId);

    List<MatchResult> findByRoomId(String roomId);

    Optional<MatchResult> findByRoomIdAndUserId(String roomId, String userId);

    long countByRoomIdAndFinishedTrue(String roomId);
}