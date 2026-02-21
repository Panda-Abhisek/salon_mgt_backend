package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Services;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicesRepository extends JpaRepository<Services, Long> {

    long countBySalonAndActiveTrue(Salon salon);

    // For internal logic (update, delete, ownership checks)
    List<Services> findBySalon(Salon salon);

    // For validation (create)
    boolean existsBySalonAndServiceName(Salon salon, String serviceName);

    @Query("""
                select count(s) > 0
                from Services s
                join s.staff st
                where s.serviceId = :serviceId
                  and st.userId = :staffId
            """)
    boolean existsStaffAssignment(
            @Param("serviceId") Long serviceId,
            @Param("staffId") Long staffId
    );

    // For API listing (N+1 safe)
    @Query("""
                select new com.panda.salon_mgt_backend.payloads.ServiceResponse(
                    s.serviceId,
                    s.serviceName,
                    s.servicePrice,
                    s.durationMinutes,
                    s.active
                )
                from Services s
                where s.salon = :salon
                order by s.serviceName
            """)
    List<ServiceResponse> findServiceResponsesBySalon(@Param("salon") Salon salon);

    Optional<Services> findByServiceIdAndSalon(Long serviceId, Salon salon);

    List<Services> findBySalonAndActiveTrue(Salon salon);

    @Query("""
                SELECT s FROM Services s
                LEFT JOIN FETCH s.staff
                WHERE s.serviceId = :serviceId
                  AND s.salon = :salon
            """)
    Optional<Services> findByServiceIdAndSalonWithStaff(
            @Param("serviceId") Long serviceId,
            @Param("salon") Salon salon
    );

    long countBySalon(Salon salon);
}
