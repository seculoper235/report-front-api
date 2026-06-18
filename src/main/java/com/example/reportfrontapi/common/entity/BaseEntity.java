package com.example.reportfrontapi.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "crt_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "crt_by", nullable = false)
    private Long createdBy;

    @Column(name = "udt_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "udt_by", nullable = false)
    private Long updatedBy;
}
