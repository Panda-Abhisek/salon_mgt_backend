package com.panda.salon_mgt_backend.payloads;

import java.util.List;

public record AdminDashboardResponse(
    long totalBookings,
    long todayBookings,
    long upcomingBookings,
    long completedBookings,
    long cancelledBookings,
    long noShowBookings,
    double totalRevenue,
    long activeServices,
    long staffCount,
    List<BookingResponse> recentBookings
) {}
