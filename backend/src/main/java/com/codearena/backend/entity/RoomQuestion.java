package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;

@Entity
@Table(name = "room_questions")
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class RoomQuestion  extends Auditable<String> implements Serializable {

    @ManyToOne
    private Room room;

    @ManyToOne
    private CodingQuestion codingQuestion;

    @ManyToOne
    private McqQuestion mcqQuestion;

    private int questionOrder;
}

