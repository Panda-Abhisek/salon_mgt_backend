package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.models.BookingRange;
import com.panda.salon_mgt_backend.models.BookingStatus;
import com.panda.salon_mgt_backend.payloads.*;
import com.panda.salon_mgt_backend.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SALON_ADMIN')")
    public ResponseEntity<BookingResponse> create(
            @RequestBody CreateBookingRequest request,
            Authentication auth
    ) {
        BookingResponse booking = bookingService.createBooking(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/salon")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getSalonBookings(
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.getSalonBookings(auth));
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('USER', 'SALON_ADMIN')")
    public ResponseEntity<List<TimeSlot>> getAvailability(
            @RequestParam Long staffId,
            @RequestParam Long serviceId,
            @RequestParam LocalDate date,
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.getAvailability(staffId, serviceId, date, auth));
    }

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'SALON_ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, auth));
    }

    @PatchMapping("/{bookingId}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'SALON_ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.completeBooking(bookingId, auth));
    }

    @PatchMapping("/{bookingId}/no-show")
    @PreAuthorize("hasAnyRole('STAFF', 'SALON_ADMIN')")
    public ResponseEntity<BookingResponse> markNoShow(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.markNoShow(bookingId, auth));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('SALON_ADMIN', 'STAFF')")
    public ResponseEntity<List<BookingResponse>> getTodayBookings(Authentication auth) {
        return ResponseEntity.ok(bookingService.getTodayBookings(auth));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SALON_ADMIN', 'STAFF')")
    public ResponseEntity<List<BookingResponse>> getStaffBookings(
            @PathVariable Long staffId,
            @RequestParam LocalDate date,
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.getStaffBookings(staffId, date, auth));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','STAFF','SALON_ADMIN')")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        BookingResponse booking = bookingService.fetchOwnedBooking(bookingId, auth);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','STAFF','SALON_ADMIN')")
    public ResponseEntity<PageResponse<BookingResponse>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingRange range,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                bookingService.getBookings(page, size, status, search, range, from, to, auth)
        );
    }

    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(
            Authentication auth
    ) {
        return ResponseEntity.ok(bookingService.getAdminDashboard(auth));
    }

}
