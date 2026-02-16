package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.SalonCreateRequest;
import com.panda.salon_mgt_backend.payloads.SalonResponse;
import com.panda.salon_mgt_backend.services.SalonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salons")
@RequiredArgsConstructor
public class SalonController {

    private final SalonService salonService;

    @PostMapping
    public ResponseEntity<SalonResponse> createSalon(
            @RequestBody SalonCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salonService.createSalon(request, authentication));
    }

    @GetMapping("/me")
    public ResponseEntity<SalonResponse> getMySalon(Authentication authentication) {
        return ResponseEntity.ok(salonService.getMySalon(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<SalonResponse> updateMySalon(
            @RequestBody SalonCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(salonService.updateMySalon(request, authentication));
    }
}
