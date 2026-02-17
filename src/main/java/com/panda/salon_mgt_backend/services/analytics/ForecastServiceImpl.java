package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.ForecastPointDTO;
import com.panda.salon_mgt_backend.repositories.BookingRepository;
import com.panda.salon_mgt_backend.services.SalonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private final BookingRepository bookingRepository;
    private final SalonService salonService;

    @Override
    public List<ForecastPointDTO> bookingForecast(Authentication auth) {

        Salon salon = salonService.getMySalonEntity(auth);

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // last 14 days window
        LocalDate fromDate = today.minusDays(14);

        Instant from = fromDate.atStartOfDay(zone).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<Object[]> rows = bookingRepository.bookingsPerDay(
                salon.getSalonId(),
                from,
                to
        );

        // map actual counts
        Map<LocalDate, Long> history = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r[0]).toLocalDate(),
                        r -> ((Number) r[1]).longValue()
                ));

        // build last 7 days values
        List<Long> last7 = new ArrayList<>();
        for (int i = 7; i >= 1; i--) {
            LocalDate d = today.minusDays(i);
            last7.add(history.getOrDefault(d, 0L));
        }

        long avg = Math.round(
                last7.stream().mapToLong(Long::longValue).average().orElse(0)
        );

        // generate forecast for next 7 days
        List<ForecastPointDTO> forecast = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            forecast.add(new ForecastPointDTO(
                    today.plusDays(i),
                    avg
            ));
        }

        return forecast;
    }
}
