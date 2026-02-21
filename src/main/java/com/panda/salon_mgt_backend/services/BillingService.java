package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import org.springframework.security.core.Authentication;

public interface BillingService {
    BillingTransaction charge(Authentication auth, Plan plan);
}