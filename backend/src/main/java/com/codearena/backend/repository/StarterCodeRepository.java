package com.codearena.backend.repository;

import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.StarterCode;
import com.codearena.backend.utils.constant.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarterCodeRepository extends JpaRepository<StarterCode,String> {
    List<StarterCode> findByCodingQuestionId(String questionId);

    Optional<StarterCode> findByCodingQuestionIdAndLanguage(String questionId, Language language);
}
