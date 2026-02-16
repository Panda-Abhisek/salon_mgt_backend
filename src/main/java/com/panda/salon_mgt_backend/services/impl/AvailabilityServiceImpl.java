package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.Booking;
import com.panda.salon_mgt_backend.models.Services;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.TimeSlot;
import com.panda.salon_mgt_backend.repositories.BookingRepository;
import com.panda.salon_mgt_backend.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final BookingRepository bookingRepository;

    @Override
    public List<TimeSlot> getAvailableSlots(
            User staff,
            Services service,
            LocalDate date
    ) {
        ZoneId salonZone = ZoneId.of("Asia/Kolkata");

        // Convert date to start/end of working day in salon timezone
        Instant dayStart = date
                .atTime(9, 0)
                .atZone(salonZone)
                .toInstant();

        Instant dayEnd = date
                .atTime(21, 0)
                .atZone(salonZone)
                .toInstant();

        // Fetch existing bookings
        List<Booking> bookings =
                bookingRepository.findOverlappingBookings(
                        staff.getUserId(),
                        dayStart,
                        dayEnd
                );

        List<TimeSlot> slots = new ArrayList<>();
        Instant cursor = dayStart;

        for (Booking b : bookings) {

            if (cursor.isBefore(b.getStartTime())) {
                slots.add(new TimeSlot(
                        cursor.atZone(salonZone).toOffsetDateTime(),
                        b.getStartTime().atZone(salonZone).toOffsetDateTime()
                ));
            }

            cursor = b.getEndTime();
        }

        if (cursor.isBefore(dayEnd)) {
            slots.add(new TimeSlot(
                    cursor.atZone(salonZone).toOffsetDateTime(),
                    dayEnd.atZone(salonZone).toOffsetDateTime()
            ));
        }

        return slots;
    }

}
