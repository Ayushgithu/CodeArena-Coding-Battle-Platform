package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import com.codearena.backend.utils.constant.Difficulty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class McqQuestion extends Auditable<String> implements Serializable {

    private String title;
    @Column(columnDefinition = "TEXT") // ✅ allow longer descriptions
    private String description;
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    private int points;
    // ✅ Time limit to answer (in seconds)
    private int timeLimit;
    // ✅ Add relation to tags
    @OneToMany(mappedBy = "mcqQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<McqQuestionTag> tags;
}

