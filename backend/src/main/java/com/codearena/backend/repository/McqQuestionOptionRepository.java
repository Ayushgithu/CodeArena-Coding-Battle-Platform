package com.codearena.backend.repository;

import com.codearena.backend.entity.McqQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface McqQuestionOptionRepository extends JpaRepository<McqQuestionOption,String> {
    @Transactional(readOnly = true)
    List<McqQuestionOption> findByMcqQuestionId(String mcqQuestionId);
    void deleteByMcqQuestionId(String mcqQuestionId);


}
