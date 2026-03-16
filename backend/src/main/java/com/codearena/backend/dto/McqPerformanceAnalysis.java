package com.codearena.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McqPerformanceAnalysis {
    private int totalQuestions;
    private int correctAnswers;
    private double accuracy;
    private double averageTimePerQuestion;
    private List<String> weakTopics;
    private List<String> strongTopics;
    private Map<String, TopicPerformance> topicPerformance;
    private Map<String, DifficultyPerformance> difficultyPerformance;
    private String recommendations;
}