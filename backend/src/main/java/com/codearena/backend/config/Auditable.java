package com.codearena.backend.config;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable<U> {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @CreatedBy
    protected U createdBy;
    @CreatedDate
    @Temporal(TIMESTAMP)
    protected Date creationDate;
    @LastModifiedBy
    protected U lastModifiedBy;
    @LastModifiedDate
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;
    @Column(nullable = false,columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted = false;

}

