package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.AlreadyExistsException;
import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.exceptions.InactiveException;
import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.*;
import com.panda.salon_mgt_backend.repositories.BookingRepository;
import com.panda.salon_mgt_backend.repositories.ServicesRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.services.AvailabilityService;
import com.panda.salon_mgt_backend.services.BookingService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.TenantGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.panda.salon_mgt_backend.models.BookingStatus.COMPLETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ServicesRepository servicesRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final TenantGuard tenantGuard;
    private final TenantContext tenantContext;

    private void assertTransitionAllowed(
            BookingStatus from,
            BookingStatus to
    ) {
        switch (from) {
            case PENDING -> {
                if (to != BookingStatus.CONFIRMED &&
                        to != BookingStatus.CANCELLED) {
                    throw new CanNotException("Invalid booking transition");
                }
            }
            case CONFIRMED -> {
                if (to != BookingStatus.CANCELLED &&
                        to != COMPLETED &&
                        to != BookingStatus.NO_SHOW) {
                    throw new CanNotException("Invalid booking transition");
                }
            }
            default -> throw new AlreadyExistsException(
                    "Booking is already in a terminal state"
            );
        }
    }

    private User resolveCustomer(CreateBookingRequest req, Authentication auth) {

        User authenticatedUser = tenantContext.getCurrentUser(auth);

        boolean isAdmin = authenticatedUser.hasRole("ROLE_SALON_ADMIN");
        boolean isUser = authenticatedUser.hasRole("ROLE_USER");

        if (isUser) {
            if (!authenticatedUser.isEnabled()) {
                throw new InactiveException("User account is disabled");
            }
            return authenticatedUser;
        }

        if (isAdmin) {

            if (req.customerId() == null) {
                throw new CanNotException("Customer must be selected");
            }

            User customer = userRepository
                    .findByIdWithRoles(req.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            if (!customer.isEnabled()) {
                throw new InactiveException("Customer is disabled");
            }

            if (!customer.hasRole("ROLE_USER")) {
                throw new CanNotException("Invalid customer role");
            }

            return customer;
        }

        throw new CanNotException("Not allowed to create booking");
    }

    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest req,
                                         Authentication auth) {

        User customer = resolveCustomer(req, auth);

        Services service = servicesRepository
                .findById(req.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        tenantGuard.assertServiceBelongsToTenant(service, auth);

        if (!service.isActive()) {
            throw new InactiveException("Service is inactive");
        }

        User staff = userRepository
                .findById(req.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        tenantGuard.assertStaffBelongsToTenant(staff, auth);

        if (!staff.isEnabled()) {
            throw new InactiveException("Staff is inactive");
        }

        if (!service.getStaff().contains(staff)) {
            throw new CanNotException("Staff not assigned to service");
        }

        Instant start = req.startTime();
        Instant end = start.plusSeconds(service.getDurationMinutes()*60L);

        if (start.isBefore(Instant.now())) {
            throw new CanNotException("Cannot book in the past");
        }

        List<Booking> conflicts =
                bookingRepository.findOverlappingBookings(
                        staff.getUserId(),
                        start,
                        end
                );

        if (!conflicts.isEmpty()) {
            throw new AlreadyExistsException("Staff already booked for this time");
        }

        Booking booking = new Booking();
        booking.setSalon(tenantContext.getSalon(auth)); // 🔥 better than getMySalonEntity
        booking.setService(service);
        booking.setStaff(staff);
        booking.setCustomer(customer);        // 🔥 required
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus(BookingStatus.CONFIRMED);

        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponse> getStaffBookings(Long staffId, LocalDate date, Authentication auth) {

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        tenantGuard.assertStaffBelongsToTenant(staff, auth);

        return bookingRepository
                .findByStaffUserIdAndStartTimeBetweenOrderByStartTimeAsc(
                        staffId,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getBookings(
            int page,
            int size,
            BookingStatus status,
            String search,
            BookingRange range,
            LocalDate from,
            LocalDate to,
            Authentication auth
    ) {
        if (search != null && search.isBlank()) {
            search = null;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "startTime")
        );

        /* ---------- Time boundaries ---------- */

        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();

        Instant fromInstant = null;
        Instant toInstant = null;
        if (from != null) {
            fromInstant = from.atStartOfDay(zone).toInstant();
        }

        if (to != null) {
            toInstant = to.plusDays(1).atStartOfDay(zone).toInstant();
        }
        boolean hasFrom = fromInstant != null;
        boolean hasTo = toInstant != null;

        Instant startOfToday = LocalDate.now(zone)
                .atStartOfDay(zone)
                .toInstant();

        Instant endOfToday = LocalDate.now(zone)
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant();

        /* ---------- Convert RANGE → FLAGS ---------- */

        boolean today = range == BookingRange.TODAY;
        boolean upcoming = range == BookingRange.UPCOMING;
        boolean past = range == BookingRange.PAST;

        User user = tenantContext.getCurrentUser(auth);
        Page<Booking> bookingsPage;

        /* ---------- SALON ADMIN ---------- */
        if (user.hasRole("ROLE_SALON_ADMIN")) {
            Salon salon = tenantContext.getSalon(auth);

            bookingsPage = (search == null)
                    ? bookingRepository.findAdminBookingsNoSearch(
                    salon, status,
                    today, upcoming, past,
                    now, startOfToday, endOfToday, hasFrom, fromInstant,
                    hasTo, toInstant, pageable
            )
                    : bookingRepository.findAdminBookingsWithSearch(
                    salon, status, search,
                    today, upcoming, past,
                    now, startOfToday, endOfToday, hasFrom, fromInstant,
                    hasTo, toInstant, pageable
            );
        }

        /* ---------- STAFF ---------- */
        else if (user.hasRole("ROLE_STAFF")) {

            bookingsPage = (search == null)
                    ? bookingRepository.findStaffBookingsNoSearch(
                    user, status,
                    today, upcoming, past,
                    now, startOfToday, endOfToday,
                    pageable
            )
                    : bookingRepository.findStaffBookingsWithSearch(
                    user, status, search,
                    today, upcoming, past,
                    now, startOfToday, endOfToday,
                    pageable
            );
        }

        /* ---------- CUSTOMER ---------- */
        else {

            bookingsPage = (search == null)
                    ? bookingRepository.findUserBookingsNoSearch(
                    user, status,
                    today, upcoming, past,
                    now, startOfToday, endOfToday,
                    pageable
            )
                    : bookingRepository.findUserBookingsWithSearch(
                    user, status, search,
                    today, upcoming, past,
                    now, startOfToday, endOfToday,
                    pageable
            );
        }

        Page<BookingResponse> mapped = bookingsPage.map(this::toResponse);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

    @Override
    public List<TimeSlot> getAvailability(
            Long staffId,
            Long serviceId,
            LocalDate date,
            Authentication auth
    ) {

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        tenantGuard.assertStaffBelongsToTenant(staff, auth);

        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        tenantGuard.assertServiceBelongsToTenant(service, auth);

        if (!service.isActive()) {
            throw new IllegalStateException("Service is inactive");
        }

        if (!staff.isEnabled()) {
            throw new IllegalStateException("Staff is disabled");
        }

        boolean assigned = servicesRepository.existsStaffAssignment(serviceId, staffId);

        if (!assigned) {
            throw new IllegalStateException("Staff does not serve this service");
        }

        return availabilityService.getAvailableSlots(staff, service, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getTodayBookings(Authentication auth) {

        Salon salon = tenantContext.resolveSalonForRead(auth);

        LocalDate today = LocalDate.now();

        ZoneId zone = ZoneId.systemDefault();

        Instant startOfDay = today
                .atStartOfDay(zone)
                .toInstant();

        Instant endOfDay = today
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant();

        return bookingRepository
                .findBySalonSalonIdAndStartTimeBetweenOrderByStartTimeDesc(
                        salon.getSalonId(),
                        startOfDay,
                        endOfDay
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getUpcomingTenantBookings(Authentication auth) {
        Salon salon = tenantContext.getSalon(auth);

        return bookingRepository
                .findBySalonSalonIdAndStartTimeBetween(
                        salon.getSalonId(),
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(30)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long bookingId, Authentication auth) {
        Booking booking = getOwnedBooking(bookingId, auth);

        assertTransitionAllowed(
                booking.getStatus(),
                BookingStatus.CANCELLED
        );

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(booking);
    }

    @Transactional
    @Override
    public BookingResponse completeBooking(Long bookingId, Authentication auth) {
        Booking booking = getOwnedBooking(bookingId, auth);

        assertTransitionAllowed(booking.getStatus(), COMPLETED);

        booking.setStatus(COMPLETED);
        booking.setCompletedAt(Instant.now());

        validateCompletedBooking(booking);

        return toResponse(booking);
    }

    private void validateCompletedBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.COMPLETED
                && booking.getCompletedAt() == null) {
            throw new CanNotException(
                    "completedAt must be set when booking is completed"
            );
        }
    }

    @Transactional
    @Override
    public BookingResponse markNoShow(Long bookingId, Authentication auth) {
        Booking booking = getOwnedBooking(bookingId, auth);

        assertTransitionAllowed(
                booking.getStatus(),
                BookingStatus.NO_SHOW
        );

        booking.setStatus(BookingStatus.NO_SHOW);
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse fetchOwnedBooking(Long bookingId, Authentication auth) {

        User user = tenantContext.getCurrentUser(auth);

        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (user.hasRole("ROLE_SALON_ADMIN")) {
            assertAdminOwnsBooking(booking, auth);
        }
        else if (user.hasRole("ROLE_STAFF")) {
            if (!booking.getStaff().equals(user)) {
                throw new CanNotException("Not your booking");
            }
        }
        else {
            if (!booking.getCustomer().equals(user)) {
                throw new CanNotException("Not your booking");
            }
        }

        return toResponse(booking);
    }

    private Booking getOwnedBooking(
            Long bookingId,
            Authentication auth
    ) {
        User user = tenantContext.getCurrentUser(auth);

        Booking booking = bookingRepository
                .findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // SALON ADMIN → salon scoped
        if (user.hasRole("ROLE_SALON_ADMIN")) {
            assertAdminOwnsBooking(booking, auth);
        }
        // STAFF → must be assigned to booking
        else if (user.hasRole("ROLE_STAFF")) {
            if (!booking.getStaff().getUserId().equals(user.getUserId())) {
                throw new CanNotException("Not your assigned booking");
            }
        }
        // USER → customer ownership
        else {
            if (!booking.getCustomer().getUserId().equals(user.getUserId())) {
                throw new CanNotException("Not your booking");
            }
        }

        return booking;
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getService().getServiceId(),
                booking.getService().getServiceName(),
                booking.getStaff().getUserId(),
                booking.getStaff().getUserName(),
                booking.getCustomer().getUserId(),
                booking.getCustomer().getUserName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard(Authentication auth) {
        User user = tenantContext.getCurrentUser(auth);

        if (!user.hasRole("ROLE_SALON_ADMIN")) {
            throw new AccessDeniedException("Admin access required");
        }

        Salon salon = tenantContext.getSalon(auth);

        long totalBookings = bookingRepository.countBySalon(salon);

        long completed = bookingRepository
                .countBySalonAndStatus(salon, BookingStatus.COMPLETED);

        long cancelled = bookingRepository
                .countBySalonAndStatus(salon, BookingStatus.CANCELLED);

        long noShow = bookingRepository
                .countBySalonAndStatus(salon, BookingStatus.NO_SHOW);

        long confirmed = bookingRepository
                .countBySalonAndStatus(salon, BookingStatus.CONFIRMED);

        // Today
        Instant startOfToday = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Instant endOfToday = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        long todayBookings = bookingRepository
                .countBookingsBetween(salon, startOfToday, endOfToday);

        Double revenue = bookingRepository.calculateTotalRevenue(salon);

        long activeServices =
                servicesRepository.countBySalonAndActiveTrue(salon);

        long staffCount =
                userRepository.countStaffBySalon(salon);

        List<BookingResponse> recentBookings =
                bookingRepository
                        .findTop5BySalonOrderByStartTimeDesc(salon)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new AdminDashboardResponse(
                totalBookings,
                todayBookings,
                confirmed,
                completed,
                cancelled,
                noShow,
                revenue != null ? revenue : 0.0,
                activeServices,
                staffCount,
                recentBookings
        );
    }

    private void assertAdminOwnsBooking(Booking booking, Authentication auth) {
        tenantGuard.assertBookingInTenant(booking, auth);
    }

}
