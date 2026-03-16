package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
public class MatchResult extends Auditable<String>  implements Serializable {

    @ManyToOne
    private Room room;

    @ManyToOne
    private User user;
    private int score;          // total points
    private long totalTime;     // total time taken
    private boolean winner;     // true if this user won
    private boolean finished; // user clicked END or auto-ended
    private LocalDateTime finishedAt;

}

