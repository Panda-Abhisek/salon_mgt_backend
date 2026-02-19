package com.panda.salon_mgt_backend.utils;

import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.repositories.SalonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TenantContext {

    private final AuthUtils authUtils;
    private final SalonRepository salonRepository;

    /**
     * Returns the salon entity for the current tenant.
     * Works for ADMIN (owner) and STAFF (assigned salon).
     */
    @Transactional(readOnly = true)
    public Salon getSalon(Authentication auth) {
        User user = authUtils.getCurrentUser(auth);

        // SALON ADMIN → owns salon
        if (user.getSalon() != null) {
            return user.getSalon();
        }

        // STAFF → belongs to salon
        if (user.getStaffSalon() != null) {
            return user.getStaffSalon();
        }

        throw new CanNotException("No tenant associated with this user");
    }

    /**
     * Lightweight version when you only need ID
     */
    @Transactional(readOnly = true)
    public Long getSalonId(Authentication auth) {
        return getSalon(auth).getSalonId();
    }

    /**
     * Useful for guards later
     */
    public boolean isSalonAdmin(Authentication auth) {
        User user = authUtils.getCurrentUser(auth);
        return user.getSalon() != null;
    }

    public User getCurrentUser(Authentication auth) {
        return authUtils.getCurrentUser(auth);
    }

    @Transactional(readOnly = true)
    public Salon resolveSalonForRead(Authentication auth) {
        User user = getCurrentUser(auth);

        Salon salon;

        if (user.hasRole("ROLE_SALON_ADMIN")) {
            salon = getSalon(auth);
        }
        else if (user.hasRole("ROLE_STAFF")) {
            salon = user.getStaffSalon();

            if (salon == null) {
                throw new ResourceNotFoundException("Staff not assigned to any salon");
            }
        }
        else {
            throw new AccessDeniedException("Not allowed");
        }
        return salon;
    }
}

