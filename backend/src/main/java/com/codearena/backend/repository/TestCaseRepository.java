package com.codearena.backend.repository;

import com.codearena.backend.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase,String> {
    List<TestCase> findByCodingQuestionIdOrderByOrderIndexAsc(String codingQuestionId);
   // List<TestCase> findByQuestion_Id(String questionId);
    void deleteByCodingQuestionId(String codingQuestionId);

    List<TestCase> findByCodingQuestionId(String codingQuestionId);

     Optional<TestCase> findFirstByCodingQuestionIdAndIsSampleTrue(String codingQuestionId);
}
