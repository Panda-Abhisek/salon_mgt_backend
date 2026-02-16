package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.SalonCreateRequest;
import com.panda.salon_mgt_backend.payloads.SalonResponse;
import org.springframework.security.core.Authentication;

public interface SalonService {
    SalonResponse createSalon(SalonCreateRequest request, Authentication auth);
    SalonResponse getMySalon(Authentication auth);
    SalonResponse updateMySalon(SalonCreateRequest request, Authentication auth);
    // for internal domain use
    Salon getMySalonEntity(Authentication auth);
}
