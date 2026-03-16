package com.codearena.backend.repository;

import com.codearena.backend.entity.CodingQuestionTags;
import com.codearena.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CodingQuestionTagsRepository extends JpaRepository<CodingQuestionTags,String> {
    List<CodingQuestionTags> findByCodingQuestionId(String codingQuestionId);
    // Delete all tags by coding question ID
    @Transactional
    void deleteByCodingQuestion_Id(String codingQuestionId);
}
