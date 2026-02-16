package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.Services;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.TimeSlot;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    List<TimeSlot> getAvailableSlots(
            User staff,
            Services service,
            LocalDate date
    );
}
