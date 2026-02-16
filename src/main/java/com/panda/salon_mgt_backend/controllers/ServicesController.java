package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.ServiceCreateRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.ServiceUpdateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import com.panda.salon_mgt_backend.services.ServicesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salons/services")
@RequiredArgsConstructor
public class ServicesController {

    private final ServicesService servicesService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getMyServices(Authentication authentication) {
        List<ServiceResponse> services = servicesService.getMyServices(authentication);
        return ResponseEntity.ok(services);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceCreateRequest request,
            Authentication authentication
    ) {
        ServiceResponse response = servicesService.createService(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceUpdateRequest request,
            Authentication authentication
    ) {
        ServiceResponse response =
                servicesService.updateService(serviceId, request, authentication);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{serviceId}/deactivate")
    public ResponseEntity<ServiceResponse> deactivateService(
            @PathVariable Long serviceId,
            Authentication authentication
    ) {
        ServiceResponse response =
                servicesService.deactivateService(serviceId, authentication);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{serviceId}/reactivate")
    public ResponseEntity<ServiceResponse> reactivateService(
            @PathVariable Long serviceId,
            Authentication authentication
    ) {
        ServiceResponse response =
                servicesService.reactivateService(serviceId, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{serviceId}/staff")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<StaffResponse> getServiceStaff(
            @PathVariable Long serviceId,
            Authentication auth
    ) {
        return servicesService.getStaffForService(serviceId, auth);
    }

//    @PutMapping("/{serviceId}/staff")
//    @PreAuthorize("hasRole('SALON_ADMIN')")
//    public List<StaffResponse> assignStaffToService(
//            @PathVariable Long serviceId,
//            @RequestBody AssignStaffRequest request,
//            Authentication auth
//    ) {
//        return servicesService.assignStaffToService(serviceId, request, auth);
//    }

}
