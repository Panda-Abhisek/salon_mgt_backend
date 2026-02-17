package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.payloads.ForecastPointDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ForecastService {
    List<ForecastPointDTO> bookingForecast(Authentication auth);
}
