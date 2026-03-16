package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class Room extends Auditable<String> implements Serializable {
    @ManyToOne
    private User joinBy;
    @ManyToOne
    private User madeBy;

    @Column(unique = true, nullable = false)
    private int roomCode;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private DropList questionType;
    private int noOfQuestion;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;  //if mcq then difficulty null

    private LocalDateTime expiryTime; // starts when both players join

    private int expiryDuration = 30; // default 30 min, can be 60

    private LocalDateTime startedAt; // when both players join

    private LocalDateTime endedAt; // when game ends
    private boolean madeBySubmitted = false;
    private boolean joinBySubmitted = false;

    private LocalDateTime madeBySubmitTime;
    private LocalDateTime joinBySubmitTime;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomQuestion> roomQuestions;

}