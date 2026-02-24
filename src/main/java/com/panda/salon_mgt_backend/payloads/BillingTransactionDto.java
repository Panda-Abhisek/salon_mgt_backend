package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.PlanType;

import java.time.Instant;

public record BillingTransactionDto(
        Long id,
        Long salonId,
        PlanType plan,
        BillingStatus status,
        String orderId,
        Instant createdAt
) {
    public static BillingTransactionDto from(BillingTransaction tx) {
        return new BillingTransactionDto(
                tx.getId(),
                tx.getSalon().getSalonId(),
                tx.getPlan(),
                tx.getStatus(),
                tx.getExternalOrderId(),
                tx.getCreatedAt()
        );
    }
}