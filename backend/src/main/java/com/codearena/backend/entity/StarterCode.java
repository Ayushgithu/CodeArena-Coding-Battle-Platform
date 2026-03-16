package com.codearena.backend.entity;


import com.codearena.backend.config.Auditable;
import com.codearena.backend.utils.constant.Language;
import jakarta.persistence.*;
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
public class StarterCode extends Auditable<String> implements Serializable {
    @ManyToOne
    private CodingQuestion codingQuestion;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Language language;
    private String version;
    @Column(name = "code_template")
    private String codeTemplate;
}
