package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
public class CodingQuestionTags extends Auditable<String> implements Serializable {
    @ManyToOne
    @JoinColumn(name = "coding_question_id")
    private CodingQuestion codingQuestion;
    private String  name;
}
