package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.models.TrendRange;
import com.panda.salon_mgt_backend.payloads.LeaderboardItemDTO;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    List<TrendPointDTO> getBookingTrend(
            Authentication auth,
            TrendRange range,
            LocalDate from,
            LocalDate to
    );

    List<TrendPointDTO> getRevenueTrend(
            Authentication auth,
            TrendRange range,
            LocalDate from,
            LocalDate to
    );

    List<LeaderboardItemDTO> getTopStaff(Authentication auth);

    List<LeaderboardItemDTO> getTopServices(Authentication auth);
}
