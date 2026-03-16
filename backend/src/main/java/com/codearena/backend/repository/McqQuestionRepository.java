package com.codearena.backend.repository;

import com.codearena.backend.entity.McqQuestion;
import com.codearena.backend.utils.constant.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqQuestionRepository extends JpaRepository<McqQuestion, String> {
    List<McqQuestion> findByDifficulty(Difficulty difficulty);

    @Query("SELECT q FROM McqQuestion q JOIN q.tags t WHERE q.difficulty = :difficulty AND LOWER(t.tagName) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<McqQuestion> findByDifficultyAndTopic(@Param("difficulty") Difficulty difficulty, @Param("topic") String topic);

    @Query(value = """
    SELECT * FROM mcq_question 
    ORDER BY RAND() 
    LIMIT :limit
""", nativeQuery = true)
    List<McqQuestion> findRandom(@Param("limit") int limit);
}
