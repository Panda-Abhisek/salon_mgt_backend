package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.payloads.ServiceCreateRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.ServiceUpdateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServicesService {

    List<ServiceResponse> getMyServices(Authentication authentication);

    ServiceResponse createService(
            ServiceCreateRequest request,
            Authentication authentication
    );

    ServiceResponse updateService(
            Long serviceId,
            ServiceUpdateRequest request,
            Authentication authentication
    );

    ServiceResponse deactivateService(
            Long serviceId,
            Authentication authentication
    );

    ServiceResponse reactivateService(
            Long serviceId,
            Authentication authentication
    );

    @Transactional(readOnly = true)
    List<StaffResponse> getStaffForService(
            Long serviceId,
            Authentication auth
    );

    List<ServiceResponse> getActiveServicesForSalon(Long salonId);

    List<StaffResponse> getPublicStaffForService(Long serviceId);
//
//    @Transactional
//    List<StaffResponse> assignStaffToService(
//            Long serviceId,
//            AssignStaffRequest request,
//            Authentication auth
//    );

}
