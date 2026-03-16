package com.codearena.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mcq_question_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McqQuestionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "mcq_question_id")
    private McqQuestion mcqQuestion;

    private String tagName;
}
