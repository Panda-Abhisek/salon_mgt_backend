package com.panda.salon_mgt_backend.payloads;

import java.util.Set;

public record AssignServicesRequest(
    Set<Long> serviceIds
) {}
