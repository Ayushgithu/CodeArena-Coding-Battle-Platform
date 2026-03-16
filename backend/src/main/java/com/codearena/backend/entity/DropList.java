package com.codearena.backend.entity;

import com.codearena.backend.config.Auditable;
import jakarta.persistence.Entity;
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
public class DropList extends Auditable<String> implements Serializable {
    String labelKey;
    String optionValue;
}
