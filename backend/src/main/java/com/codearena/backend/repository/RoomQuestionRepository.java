package com.codearena.backend.repository;

import com.codearena.backend.entity.RoomQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomQuestionRepository extends JpaRepository<RoomQuestion, String> {

    List<RoomQuestion> findByRoomIdOrderByQuestionOrder(String roomId);

    List<RoomQuestion> findByRoomId(String roomId);

    @Query("SELECT COUNT(rq) > 0 FROM RoomQuestion rq WHERE rq.room.id = :roomId")
    boolean existsByRoomId(@Param("roomId") String roomId);

//    int countByRoomIdAndSolvedTrue(String roomId);

    int countByRoomId(String roomId);

    // Optional optimization method - uncomment if you want to use it
    // @Query("SELECT rq FROM RoomQuestion rq " +
    //        "LEFT JOIN FETCH rq.codingQuestion " +
    //        "LEFT JOIN FETCH rq.mcqQuestion " +
    //        "WHERE rq.room.id = :roomId " +
    //        "ORDER BY rq.questionOrder")
    // List<RoomQuestion> findByRoomIdWithQuestions(@Param("roomId") String roomId);

    // Optional method for finding next unsolved question - uncomment if you want to use it
    // @Query("SELECT rq FROM RoomQuestion rq " +
    //        "WHERE rq.room.id = :roomId " +
    //        "AND rq.questionOrder > :currentOrder " +
    //        "AND rq.solved = false " +
    //        "ORDER BY rq.questionOrder " +
    //        "LIMIT 1")
    // Optional<RoomQuestion> findNextUnsolvedQuestion(
    //         @Param("roomId") String roomId,
    //         @Param("currentOrder") int currentOrder);
}