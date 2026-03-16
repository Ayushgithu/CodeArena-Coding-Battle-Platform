package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import com.codearena.backend.utils.constant.Difficulty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "practice_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class PracticeSession extends Auditable<String> {

    @ManyToOne
    private User user;

    private String sessionCode; // Unique code for the session

    @Enumerated(EnumType.STRING)
    private Difficulty startingDifficulty;

    @Enumerated(EnumType.STRING)
    private Difficulty currentDifficulty;

    private String questionType; // "CODING" or "MCQ"

    private int maxQuestions;
    private int timeLimitMinutes; // Total time for the session

    private String topic; // Optional specific topic

    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;

    private int currentQuestionIndex; // 0-based index
    private int totalQuestionsAnswered;
    private int correctAnswers;

    @Column(columnDefinition = "TEXT")
    private String answeredQuestionIds; // JSON array of question IDs already asked

    @Column(columnDefinition = "TEXT")
    private String performanceHistory; // JSON array of performance metrics

    private double averageTimePerQuestion;
    private double accuracyPercentage;

    private boolean isCompleted;
    private boolean isExpired;

    private String currentQuestionId;

    @ElementCollection
    @CollectionTable(name = "practice_session_metrics",
            joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "metric_key")
    @Column(name = "metric_value", columnDefinition = "TEXT")
    private Map<String, String> performanceMetrics = new HashMap<>();
}