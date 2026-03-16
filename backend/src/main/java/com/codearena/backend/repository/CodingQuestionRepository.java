package com.codearena.backend.repository;

import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.utils.constant.Difficulty;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, String> {
    List<CodingQuestion> findByDifficulty(Difficulty difficulty);

    @Query("SELECT c FROM CodingQuestion c JOIN c.tags t WHERE c.difficulty = :difficulty AND LOWER(t.name) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<CodingQuestion> findByDifficultyAndTopic(@Param("difficulty") Difficulty difficulty, @Param("topic") String topic);

    @Query("""
SELECT c FROM CodingQuestion c
WHERE c.difficulty = :difficulty
ORDER BY function('RAND')
""")
    List<CodingQuestion> findRandomByDifficulty(
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable
    );
}
