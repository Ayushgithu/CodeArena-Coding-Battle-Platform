package com.codearena.backend.entity;


import com.codearena.backend.config.Auditable;
import com.codearena.backend.utils.constant.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class Submission extends Auditable<String> implements Serializable {

    @ManyToOne
    private User user;

    // ✅ NEW/UPDATED: Link to the Room (must be present)
    @ManyToOne
    private Room room;
    @ManyToOne
    private CodingQuestion question; // Link to Coding Question
    private String language; // e.g. "python", "java", "cpp"
    @Column(columnDefinition = "TEXT")
    private String sourceCode;
    private int attemptNumber;
    // MCQ Submission fields (optional - only set for MCQ type)
    @ManyToOne
    private McqQuestion mcqQuestion; // Link to MCQ Question
    private String mcqOptionId; // Stores the ID of the selected option
    private Boolean isCorrect; // Boolean result for MCQ

    private LocalDateTime submittedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status; // e.g. PENDING, ACCEPTED, WRONG_ANSWER, RUNTIME_ERROR, PARTIALLY_CORRECT

    private int score; // e.g. 100, 80, 0 etc.

    private double executionTime; // in seconds (for tie-breaking)
    private double memoryUsed; // in MB (for coding)
    @Column(columnDefinition = "TEXT")
    private String compilerMessage; // from Piston API if error occurs (for coding)
    private int passedTestCases;
    private int totalTestCases;


}