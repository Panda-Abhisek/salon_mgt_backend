package com.panda.salon_mgt_backend.configs;

import org.springframework.stereotype.Component;

@Component
public class MockPaymentProcessor {

    public boolean processPayment(Integer amount) {
        // Always succeed for now
        return true;

        // If you want drama later:
        // return Math.random() > 0.1;
    }
}