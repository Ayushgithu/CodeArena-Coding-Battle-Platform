package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;


@Entity
@Table(name = "mcq_question_options")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Where(clause = "is_deleted = 0")
// ✅ add this
@Data
public class McqQuestionOption extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(columnDefinition = "TEXT") // ✅ to allow large option text if needed
    private String optionText;
    private boolean isCorrect;
    @ManyToOne
    @JoinColumn(name = "mcq_question_id")
    @JsonIgnore // ✅ prevents infinite serialization
    private McqQuestion mcqQuestion;
}
