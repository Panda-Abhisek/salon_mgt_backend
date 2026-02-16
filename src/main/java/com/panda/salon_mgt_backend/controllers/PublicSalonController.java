package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.PublicSalonResponse;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import com.panda.salon_mgt_backend.repositories.SalonRepository;
import com.panda.salon_mgt_backend.services.ServicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicSalonController {

    private final ServicesService servicesService;
    private final SalonRepository salonRepository;

    @GetMapping("/salons")
    public ResponseEntity<List<PublicSalonResponse>> getAllSalons() {
        List<PublicSalonResponse> salons = salonRepository.findAllPublic()
                .stream()
                .map(s -> new PublicSalonResponse(
                        s.getSalonId(),
                        s.getSalonName(),
                        s.getSalonAddress()
                ))
                .toList();

        return ResponseEntity.ok(salons);
    }

    @GetMapping("/salons/{salonId}")
    public ResponseEntity<PublicSalonResponse> getSalonById(@PathVariable Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        return ResponseEntity.ok(
                new PublicSalonResponse(
                        salon.getSalonId(),
                        salon.getSalonName(),
                        salon.getSalonAddress()
                )
        );
    }

    @GetMapping("/salons/{salonId}/services")
    public ResponseEntity<List<ServiceResponse>> getPublicServices(@PathVariable Long salonId) {
        return ResponseEntity.ok(
                servicesService.getActiveServicesForSalon(salonId)
        );
    }

    @GetMapping("/services/{serviceId}/staff")
    public ResponseEntity<List<StaffResponse>> getPublicStaffForService(
            @PathVariable Long serviceId
    ) {
        return ResponseEntity.ok(
                servicesService.getPublicStaffForService(serviceId)
        );
    }

}

