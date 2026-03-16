package com.codearena.backend.repository;

import com.codearena.backend.entity.McqQuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqQuestionTagsRepository extends JpaRepository<McqQuestionTag, String> {
    List<McqQuestionTag> findByMcqQuestionId(String mcqQuestionId);
}
