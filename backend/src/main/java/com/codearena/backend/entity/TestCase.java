package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class TestCase extends Auditable<String> implements Serializable {

    @ManyToOne
    private CodingQuestion codingQuestion;
    private String inputData;
    private String expectedOutput;
    private boolean isSample; // true for public sample test cases
    private int orderIndex;
    private String explanation;
}

