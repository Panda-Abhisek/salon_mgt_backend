package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.services.impl.BillingPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingPortalController {

    private final BillingPortalService portalService;

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> createPortal(Authentication auth) {
        String url = portalService.createPortalSession(auth);
        return ResponseEntity.ok(Map.of("url", url));
    }
}