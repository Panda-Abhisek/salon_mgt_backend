package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    // Plan
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private Instant startDate;
    private Instant endDate;

    // Future billing integrations
    // First successful payment intent (used for refunds/support)
    private String externalPaymentId; // Stripe/Razorpay

    // Stripe recurring linkage
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd = false;

    // Dunning intelligence
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_payment_failure_at")
    private Instant lastPaymentFailureAt;

    @Column(name = "delinquent")
    private Boolean delinquent = false;
}