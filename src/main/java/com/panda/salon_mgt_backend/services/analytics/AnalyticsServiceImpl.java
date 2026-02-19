package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.TrendRange;
import com.panda.salon_mgt_backend.payloads.LeaderboardItemDTO;
import com.panda.salon_mgt_backend.repositories.BookingRepository;
import com.panda.salon_mgt_backend.services.SalonService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BookingRepository bookingRepository;
    private final SalonService salonService;
    private final TenantContext tenantContext;

    @Override
    public List<TrendPointDTO> getBookingTrend(Authentication auth, TrendRange range, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        if (range != null && range != TrendRange.CUSTOM) {
            switch (range) {
                case LAST_7_DAYS -> {
                    from = today.minusDays(6);
                    to = today;
                }
                case LAST_30_DAYS -> {
                    from = today.minusDays(29);
                    to = today;
                }
                case LAST_90_DAYS -> {
                    from = today.minusDays(89);
                    to = today;
                }
            }
        }

        Salon salon = salonService.getMySalonEntity(auth);
        ZoneId zone = ZoneId.systemDefault();

        Instant fromInstant = from != null
                ? from.atStartOfDay(zone).toInstant()
                : Instant.EPOCH;

        Instant toInstant = to != null
                ? to.plusDays(1).atStartOfDay(zone).toInstant()
                : Instant.now().plus(3650, ChronoUnit.DAYS);

        List<Object[]> rows = bookingRepository.bookingTrend(
                salon.getSalonId(),
                fromInstant,
                toInstant
        );

        return rows.stream()
                .map(r -> new TrendPointDTO(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TrendPointDTO> getRevenueTrend(
            Authentication auth,
            TrendRange range,
            LocalDate from,
            LocalDate to
    ) {
        LocalDate today = LocalDate.now();

        if (range != null && range != TrendRange.CUSTOM) {
            switch (range) {
                case LAST_7_DAYS -> {
                    from = today.minusDays(6);
                    to = today;
                }
                case LAST_30_DAYS -> {
                    from = today.minusDays(29);
                    to = today;
                }
                case LAST_90_DAYS -> {
                    from = today.minusDays(89);
                    to = today;
                }
            }
        }

        Salon salon = salonService.getMySalonEntity(auth);
        ZoneId zone = ZoneId.systemDefault();

        Instant fromInstant = from != null
                ? from.atStartOfDay(zone).toInstant()
                : Instant.EPOCH;

        Instant toInstant = to != null
                ? to.plusDays(1).atStartOfDay(zone).toInstant()
                : Instant.now().plus(3650, ChronoUnit.DAYS);

        List<Object[]> rows = bookingRepository.revenueTrend(
                salon.getSalonId(),
                fromInstant,
                toInstant
        );

        return rows.stream()
                .map(r -> new TrendPointDTO(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    @Override
    public List<LeaderboardItemDTO> getTopStaff(Authentication auth) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);
        return bookingRepository.topStaff(salon.getSalonId())
                .stream()
                .limit(5)
                .map(r -> new LeaderboardItemDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    @Override
    public List<LeaderboardItemDTO> getTopServices(Authentication auth) {
        Salon salon = salonService.getMySalonEntity(auth);

        return bookingRepository.topServices(salon.getSalonId())
                .stream()
                .limit(5)
                .map(r -> new LeaderboardItemDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }


}
