package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.models.TrendRange;
import com.panda.salon_mgt_backend.payloads.LeaderboardItemDTO;
import com.panda.salon_mgt_backend.services.analytics.AnalyticsService;
import com.panda.salon_mgt_backend.services.analytics.TrendPointDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/bookings/trend")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<TrendPointDTO> trend(
            Authentication auth,
            @RequestParam(required = false) TrendRange range,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return analyticsService.getBookingTrend(auth, range, from, to);
    }

    @GetMapping("/revenue/trend")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<TrendPointDTO> revenueTrend(
            Authentication auth,
            @RequestParam(required = false) TrendRange range,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return analyticsService.getRevenueTrend(auth, range, from, to);
    }

    @GetMapping("/leaderboard/staff")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<LeaderboardItemDTO> topStaff(Authentication auth) {
        return analyticsService.getTopStaff(auth);
    }

    @GetMapping("/leaderboard/services")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<LeaderboardItemDTO> topServices(Authentication auth) {
        return analyticsService.getTopServices(auth);
    }

}
