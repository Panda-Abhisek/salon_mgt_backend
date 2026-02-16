package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Booking;
import com.panda.salon_mgt_backend.models.BookingStatus;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStaff_UserId(Long staffId);

    List<Booking> findByCustomer_UserId(Long customerId);

    @Query("""
                select b
                from Booking b
                join fetch b.service
                join fetch b.staff
                join fetch b.customer
                where b.salon = :salon
                order by b.startTime desc
            """)
    List<Booking> findBySalonOrderByStartTimeDesc(@Param("salon") Salon salon);


    /* ---------- Conflict check ---------- */

    @Query("""
                SELECT b FROM Booking b
                WHERE b.staff.userId = :staffId
                  AND b.status = 'CONFIRMED'
                  AND b.startTime < :end
                  AND b.endTime > :start
            """)
    List<Booking> findOverlappingBookings(
            Long staffId,
            Instant start,
            Instant end
    );

    /* ---------- Salon day view ---------- */

    List<Booking> findBySalonSalonIdAndStartTimeBetween(
            Long salonId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
                select b from Booking b
                join fetch b.service
                join fetch b.staff
                join fetch b.customer
                where b.id = :id
            """)
    Optional<Booking> findWithRelations(@Param("id") Long id);

    @Query("""
                select b from Booking b
                join fetch b.service
                join fetch b.staff
                join fetch b.customer
                where b.id = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    Optional<Booking> findByIdAndSalon(
            Long bookingId,
            Salon salon
    );

    List<Booking> findBySalonSalonIdAndStartTimeBetweenOrderByStartTimeDesc(
            Long salonId,
            Instant start,
            Instant end
    );

    List<Booking> findByStaffUserIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long staffId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.salon = :salon
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
                  and (:hasFrom = false or b.startTime >= :fromInstant)
                  and (:hasTo = false or b.startTime < :toInstant)
            """)
    Page<Booking> findAdminBookingsNoSearch(
            @Param("salon") Salon salon,
            @Param("status") BookingStatus status,
            @Param("today") boolean today,
            @Param("upcoming") boolean upcoming,
            @Param("past") boolean past,
            @Param("now") Instant now,
            @Param("startOfToday") Instant startOfToday,
            @Param("endOfToday") Instant endOfToday,
            @Param("hasFrom") boolean hasFrom,
            @Param("fromInstant") Instant fromInstant,
            @Param("hasTo") boolean hasTo,
            @Param("toInstant") Instant toInstant,
            Pageable pageable
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.salon = :salon
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
                  and (
                        lower(c.userName) like concat('%', lower(:search), '%')
                     or lower(st.userName) like concat('%', lower(:search), '%')
                     or lower(s.serviceName) like concat('%', lower(:search), '%')
                  )
                  and (:hasFrom = false or b.startTime >= :fromInstant)
                  and (:hasTo = false or b.startTime < :toInstant)
            """)
    Page<Booking> findAdminBookingsWithSearch(
            Salon salon,
            BookingStatus status,
            String search,
            boolean today,
            boolean upcoming,
            boolean past,
            Instant now,
            Instant startOfToday,
            Instant endOfToday,
            @Param("hasFrom") boolean hasFrom,
            @Param("fromInstant") Instant fromInstant,
            @Param("hasTo") boolean hasTo,
            @Param("toInstant") Instant toInstant,
            Pageable pageable
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.staff = :staff
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
            """)
    Page<Booking> findStaffBookingsNoSearch(
            User staff,
            BookingStatus status,
            boolean today,
            boolean upcoming,
            boolean past,
            Instant now,
            Instant startOfToday,
            Instant endOfToday,
            Pageable pageable
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.staff = :staff
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
                  and (
                        lower(c.userName) like concat('%', lower(:search), '%')
                     or lower(st.userName) like concat('%', lower(:search), '%')
                     or lower(s.serviceName) like concat('%', lower(:search), '%')
                  )
            """)
    Page<Booking> findStaffBookingsWithSearch(
            User staff,
            BookingStatus status,
            String search,
            boolean today,
            boolean upcoming,
            boolean past,
            Instant now,
            Instant startOfToday,
            Instant endOfToday,
            Pageable pageable
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.customer = :customer
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
            """)
    Page<Booking> findUserBookingsNoSearch(
            User customer,
            BookingStatus status,
            boolean today,
            boolean upcoming,
            boolean past,
            Instant now,
            Instant startOfToday,
            Instant endOfToday,
            Pageable pageable
    );

    @Query("""
                select b from Booking b
                join b.service s
                join b.staff st
                join b.customer c
                where b.customer = :customer
                  and (:status is null or b.status = :status)
                  and (
                        (:today = false and :upcoming = false and :past = false)
                     or (:today = true and b.startTime between :startOfToday and :endOfToday)
                     or (:upcoming = true and b.startTime > :now)
                     or (:past = true and b.startTime < :now)
                  )
                  and (
                        lower(c.userName) like concat('%', lower(:search), '%')
                     or lower(st.userName) like concat('%', lower(:search), '%')
                     or lower(s.serviceName) like concat('%', lower(:search), '%')
                  )
            """)
    Page<Booking> findUserBookingsWithSearch(
            User customer,
            BookingStatus status,
            String search,
            boolean today,
            boolean upcoming,
            boolean past,
            Instant now,
            Instant startOfToday,
            Instant endOfToday,
            Pageable pageable
    );

    long countBySalon(Salon salon);

    long countBySalonAndStatus(Salon salon, BookingStatus status);

    long countBySalonAndStartTimeBetween(
            Salon salon,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
                select count(b)
                from Booking b
                where b.salon = :salon
                  and b.startTime between :start and :end
            """)
    long countBookingsBetween(
            @Param("salon") Salon salon,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
               select coalesce(sum(s.servicePrice), 0)
               from Booking b
               join b.service s
               where b.salon = :salon
                 and b.status = com.panda.salon_mgt_backend.models.BookingStatus.COMPLETED
            """)
    Double calculateTotalRevenue(@Param("salon") Salon salon);

    List<Booking> findTop5BySalonOrderByStartTimeDesc(Salon salon);

    @Query("""
                SELECT FUNCTION('DATE', b.startTime), COUNT(b)
                FROM Booking b
                WHERE b.salon.salonId = :salonId
                  AND b.startTime >= :from
                  AND b.startTime < :to
                GROUP BY FUNCTION('DATE', b.startTime)
                ORDER BY FUNCTION('DATE', b.startTime)
            """)
    List<Object[]> bookingTrend(
            Long salonId,
            Instant from,
            Instant to
    );

    @Query("""
                SELECT FUNCTION('DATE', b.startTime), COALESCE(SUM(s.servicePrice), 0)
                FROM Booking b
                JOIN b.service s
                WHERE b.salon.salonId = :salonId
                  AND b.status = com.panda.salon_mgt_backend.models.BookingStatus.COMPLETED
                  AND b.startTime >= :from
                  AND b.startTime < :to
                GROUP BY FUNCTION('DATE', b.startTime)
                ORDER BY FUNCTION('DATE', b.startTime)
            """)
    List<Object[]> revenueTrend(
            Long salonId,
            Instant from,
            Instant to
    );

    @Query("""
                SELECT st.userName, COUNT(b)
                FROM Booking b
                JOIN b.staff st
                WHERE b.salon.salonId = :salonId
                  AND b.status = com.panda.salon_mgt_backend.models.BookingStatus.COMPLETED
                GROUP BY st.userName
                ORDER BY COUNT(b) DESC
            """)
    List<Object[]> topStaff(Long salonId);


    @Query("""
                SELECT s.serviceName, COUNT(b)
                FROM Booking b
                JOIN b.service s
                WHERE b.salon.salonId = :salonId
                  AND b.status = com.panda.salon_mgt_backend.models.BookingStatus.COMPLETED
                GROUP BY s.serviceName
                ORDER BY COUNT(b) DESC
            """)
    List<Object[]> topServices(Long salonId);

}
