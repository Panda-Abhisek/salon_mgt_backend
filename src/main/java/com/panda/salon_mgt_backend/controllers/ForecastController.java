package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.ForecastPointDTO;
import com.panda.salon_mgt_backend.services.analytics.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public List<ForecastPointDTO> forecast(Authentication auth) {
        return forecastService.bookingForecast(auth);
    }
}
