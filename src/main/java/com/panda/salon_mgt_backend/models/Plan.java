package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType type;

    private String name;

    // Feature limits
    private Integer maxStaff;
    private Integer maxServices;
    private Integer maxBookings;
    private Boolean analyticsEnabled;
    private Boolean smartAlertsEnabled;

    // Future billing
    private Integer priceMonthly; // INR paise later
}