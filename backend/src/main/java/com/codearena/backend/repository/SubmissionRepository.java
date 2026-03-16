package com.codearena.backend.repository;

import com.codearena.backend.entity.Submission;
import com.codearena.backend.entity.User;
import com.codearena.backend.utils.constant.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission,String> {
    // ✅ NEW: Find all submissions for a specific room
    List<Submission> findByRoomId(String roomId);

    // ✅ NEW: Check if a user has already submitted in a room
    boolean existsByRoomIdAndUserId(String roomId, String userId);

    List<Submission> findByRoomIdAndUserId(String roomId, String userId);
    int countByUserIdAndRoomIdAndQuestionId(
            String userId,
            String roomId,
            String questionId
    );
    boolean existsByUserIdAndRoomIdAndQuestionIdAndStatus(
            String userId,
            String roomId,
            String questionId,
            SubmissionStatus status
    );


    @Query("""
SELECT s.user, SUM(s.score)
FROM Submission s
WHERE s.room.id = :roomId
AND s.status = 'ACCEPTED'
GROUP BY s.user
""")
    List<Object[]> calculateRoomScore(String roomId);

}