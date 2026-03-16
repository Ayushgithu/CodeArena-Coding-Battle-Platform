package com.codearena.backend.repository;

import com.codearena.backend.entity.PracticeSession;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, String> {

    Optional<PracticeSession> findBySessionCode(String sessionCode);

    List<PracticeSession> findByUserIdAndIsCompletedFalseAndIsExpiredFalse(String userId);

    List<PracticeSession> findByUserIdAndIsCompletedTrueOrderByStartedAtDesc(String userId);

    List<PracticeSession> findByUserId(String userId);

//    long countByUserIdAndIsCompletedTrue(String userId);

    // ✅ NEW: Find expired sessions for cleanup
    @Query("""
        SELECT ps FROM PracticeSession ps 
        WHERE ps.expiresAt < :cutoffTime 
        AND ps.isCompleted = false 
        AND ps.isExpired = false
        AND ps.isDeleted = false
    """)
    List<PracticeSession> findExpiredSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ✅ NEW: Find old completed sessions for deletion
    @Query("""
        SELECT ps FROM PracticeSession ps 
        WHERE ps.expiresAt < :cutoffTime 
        AND ps.isCompleted = true 
        AND ps.isDeleted = false
    """)
    List<PracticeSession> findOldCompletedSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Add these methods to PracticeSessionRepository.java

    /**
     * Find practice sessions within date range that are completed
     */
    List<PracticeSession> findByUserIdAndStartedAtBetweenAndIsCompletedTrue(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find sessions that are not completed, not expired, but started before cutoff time
     */
    List<PracticeSession> findByIsCompletedFalseAndIsExpiredFalseAndStartedAtBefore(
            LocalDateTime cutoffTime
    );

    /**
     * Count room questions by room ID
     */
    @Query("SELECT COUNT(rq) FROM RoomQuestion rq WHERE rq.room.id = :roomId")
    int countByRoomId(@Param("roomId") String roomId);

    /**
     * Count solved room questions by room ID
     */
//    @Query("SELECT COUNT(rq) FROM RoomQuestion rq WHERE rq.room.id = :roomId AND rq.solved = true")
//    int countByRoomIdAndSolvedTrue(@Param("roomId") String roomId);
}