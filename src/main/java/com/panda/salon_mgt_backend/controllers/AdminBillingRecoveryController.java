package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.services.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/billing-recovery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminBillingRecoveryController {

    private final BillingTransactionRepository billingRepo;
    private final BillingService billingService;

    // 🧾 List dead letters
    @GetMapping("/dead-letters")
    public List<BillingTransaction> deadLetters() {
        return billingRepo.findTop50ByStatusOrderByCreatedAtDesc(
                BillingStatus.FAILED_PERMANENT
        );
    }

    // 🔄 Manual recovery
    @PostMapping("/recover/{txId}")
    public void recover(@PathVariable Long txId) {
        BillingTransaction tx = billingRepo.findById(txId)
                .orElseThrow();

        billingService.forceRecoverTransaction(tx);
    }
}