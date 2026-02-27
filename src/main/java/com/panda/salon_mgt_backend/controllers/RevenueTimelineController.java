package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.RevenuePoint;
import com.panda.salon_mgt_backend.services.impl.RevenueTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics/revenue")
@RequiredArgsConstructor
public class RevenueTimelineController {

    private final RevenueTimelineService service;

    @GetMapping("/timeline")
    public List<RevenuePoint> timeline(
            @RequestParam(defaultValue = "30") int days
    ) {
        return service.getTimeline(days);
    }
}