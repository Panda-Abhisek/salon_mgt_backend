package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.payloads.AssignServicesRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.StaffCreateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StaffService {

    List<StaffResponse> getMyStaff(Authentication authentication);

    StaffResponse addStaff(
            StaffCreateRequest request,
            Authentication authentication
    );

    StaffResponse deactivateStaff(
            Long staffId,
            Authentication authentication
    );

    StaffResponse reactivateStaff(
            Long staffId,
            Authentication authentication
    );

    @Transactional(readOnly = true)
    List<ServiceResponse> getServicesForStaff(
            Long staffId,
            Authentication auth
    );

    StaffResponse assignServicesToStaff(
            Long staffId,
            AssignServicesRequest request,
            Authentication auth
    );
}
