package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.BookingRange;
import com.panda.salon_mgt_backend.models.BookingStatus;
import com.panda.salon_mgt_backend.payloads.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    @Transactional
    BookingResponse createBooking(CreateBookingRequest req, Authentication auth);

    List<BookingResponse> getTodayBookings(Authentication auth);

    List<BookingResponse> getSalonBookings(Authentication auth);

    @Transactional
    BookingResponse cancelBooking(Long bookingId, Authentication auth);

    @Transactional
    BookingResponse completeBooking(Long bookingId, Authentication auth);

    @Transactional
    BookingResponse markNoShow(Long bookingId, Authentication auth);

    List<TimeSlot> getAvailability(Long staffId, Long serviceId, LocalDate date, Authentication auth);

    List<BookingResponse> getStaffBookings(Long staffId, LocalDate date, Authentication auth);

    BookingResponse fetchOwnedBooking(Long bookingId, Authentication auth);

    PageResponse<BookingResponse> getBookings(int page, int size, BookingStatus status, String search,
                                              BookingRange range, LocalDate from, LocalDate to, Authentication auth);

    AdminDashboardResponse getAdminDashboard(Authentication auth);

}
