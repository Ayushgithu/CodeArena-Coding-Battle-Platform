package com.codearena.backend.entity;


import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@Where(clause = "is_deleted = 0")
public class ChatMessage extends Auditable<String> implements Serializable {
    private String content;
    @ManyToOne
    private User sender;
    @ManyToOne
    private Room room;
}
