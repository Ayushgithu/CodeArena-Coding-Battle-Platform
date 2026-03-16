package com.codearena.backend.entity;


import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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
public class UserProfile extends Auditable<String> implements Serializable {
    @OneToOne
    private User user;
    private String name;
    private int userRank;
    private String email;
    private int totalWin;
    private int totalLoss;
    private int totalBattle;
    private int score;
}
