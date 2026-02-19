package com.panda.salon_mgt_backend.utils;

import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.models.Booking;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Services;
import com.panda.salon_mgt_backend.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantGuard {

    private final TenantContext tenantContext;

    /**
     * Ensures the given staff belongs to the current tenant.
     */
    public void assertStaffBelongsToTenant(User staff, Authentication auth) {
        Salon tenantSalon = tenantContext.getSalon(auth);

        if (staff.getStaffSalon() == null ||
                !tenantSalon.getSalonId().equals(staff.getStaffSalon().getSalonId())) {
            throw new CanNotException("Staff does not belong to your salon");
        }
    }

    /**
     * Ensures an entity salon matches the tenant salon.
     */
    public void assertSalonOwnership(Salon entitySalon, Authentication auth) {
        Salon tenantSalon = tenantContext.getSalon(auth);

        if (!tenantSalon.getSalonId().equals(entitySalon.getSalonId())) {
            throw new CanNotException("Cross-tenant access denied");
        }
    }

    /**
     * Useful for bookings/services later
     */
    public void assertSameSalon(Long entitySalonId, Authentication auth) {
        Long tenantSalonId = tenantContext.getSalonId(auth);

        if (!tenantSalonId.equals(entitySalonId)) {
            throw new CanNotException("Cross-tenant access denied");
        }
    }

    public void assertBookingInTenant(Booking booking, Authentication auth) {
        Salon salon = tenantContext.getSalon(auth);
        if (!booking.getSalon().equals(salon)) {
            throw new CanNotException("Not your salon booking");
        }
    }

    public void assertServiceBelongsToTenant(Services service, Authentication auth) {
        Salon tenantSalon = tenantContext.getSalon(auth);
        if (!service.getSalon().getSalonId().equals(tenantSalon.getSalonId())) {
            throw new AccessDeniedException("Service does not belong to your salon");
        }
    }

}

