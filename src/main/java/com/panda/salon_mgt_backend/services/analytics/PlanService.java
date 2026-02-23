package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.payloads.PlanResponse;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(p -> new PlanResponse(
                        p.getType(),
                        p.getName(),
                        p.getMaxStaff(),
                        p.getMaxServices(),
                        p.getAnalyticsEnabled(),
                        p.getSmartAlertsEnabled(),
                        p.getPriceMonthly()
                ))
                .toList();
    }
}