package com.panda.salon_mgt_backend.payloads;

import java.util.List;

public record AssignStaffRequest(
        List<Long> staffIds
) {}
