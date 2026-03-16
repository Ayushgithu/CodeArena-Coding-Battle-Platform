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
@Data
@Where(clause = "is_deleted = 0")
public class CodingQuestion extends Auditable<String> implements Serializable {

    @Column(nullable = false)
    private String title;

    private String description;

    private String inputFormat;

    private String outputFormat;

    private String constraints;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int points;

    private double timeLimit; // in milliseconds or seconds

    private double memoryLimit; // in MB (optional)

    @OneToMany(mappedBy = "codingQuestion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<StarterCode> starterCodes;

    @OneToMany(mappedBy = "codingQuestion", cascade = CascadeType.ALL)
    private List<CodingQuestionTags> tags;


}
