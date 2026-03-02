package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_salon", columnList = "salonId"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_time", columnList = "createdAt")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long salonId;

    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(length = 1000)
    private String details;

    private Instant createdAt;
}