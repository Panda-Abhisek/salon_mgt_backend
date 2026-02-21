package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "billing_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant
    @ManyToOne
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    // Plan purchased
    @Enumerated(EnumType.STRING)
    private PlanType planType;

    // Billing state
    @Enumerated(EnumType.STRING)
    private BillingStatus status;

    private Integer amount; // paise later (keep int for now)

    private Instant createdAt;
    private Instant paidAt;

    // Future gateway integration
    private String externalPaymentId;
}