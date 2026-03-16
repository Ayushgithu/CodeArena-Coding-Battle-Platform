package com.codearena.backend.entity;


import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "is_deleted = 0")
@Data
public class Role extends Auditable<String> implements Serializable {

    private String name;
}
