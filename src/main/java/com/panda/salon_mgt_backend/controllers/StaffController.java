package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.AssignServicesRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.StaffCreateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import com.panda.salon_mgt_backend.services.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salons/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PreAuthorize("hasRole('SALON_ADMIN')")
    @GetMapping
    public List<StaffResponse> list(Authentication auth) {
        return staffService.getMyStaff(auth);
    }

    @PostMapping
    public StaffResponse create(
            @Valid @RequestBody StaffCreateRequest request,
            Authentication auth
    ) {
        return staffService.addStaff(request, auth);
    }

    @PatchMapping("/{id}/deactivate")
    public StaffResponse deactivate(
            @PathVariable Long id,
            Authentication auth
    ) {
        return staffService.deactivateStaff(id, auth);
    }

    @PatchMapping("/{id}/reactivate")
    public StaffResponse reactivate(
            @PathVariable Long id,
            Authentication auth
    ) {
        return staffService.reactivateStaff(id, auth);
    }

    @GetMapping("/{staffId}/services")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<ServiceResponse> getStaffServices(
            @PathVariable Long staffId,
            Authentication auth
    ) {
        return staffService.getServicesForStaff(staffId, auth);
    }

    @PutMapping("/{id}/services")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public ResponseEntity<StaffResponse> assignServices(
            @PathVariable Long id,
            @RequestBody @Valid AssignServicesRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                staffService.assignServicesToStaff(id, request, authentication)
        );
    }

}
