package com.codearena.backend.dto;


import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RoomResponseDTO {
    private String id;
    private int roomCode;
    private int expiryDuration;
    private String createdByName;
    private String joinedByName;
    private Status status;
    private String questionType;
    private LocalDateTime startedAt;
    private int noOfQuestion;
    private Difficulty difficulty;
    private Date createdAt;
    private LocalDateTime expiryTime;
    // ✅ NEW FIELD
    private String assignedQuestionId;
    private String winner;
}