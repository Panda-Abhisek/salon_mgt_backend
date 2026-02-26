package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "billing_transactions",
        indexes = {
                @Index(name = "idx_billing_status", columnList = "status"),
                @Index(name = "idx_billing_external_payment", columnList = "externalPaymentId")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BillingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    // Plan being purchased
    @Enumerated(EnumType.STRING)
    private PlanType plan;

    // Amount in paise
    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private BillingStatus status;

    // Provider metadata
    @Enumerated(EnumType.STRING)
    private BillingProviderType provider;          // RAZORPAY / STRIPE

    @Column(name = "external_payment_id")
    private String initialPaymentIntentId; // payment id
    private String externalOrderId;   // order id

    private Instant createdAt;
    private Instant completedAt;
}