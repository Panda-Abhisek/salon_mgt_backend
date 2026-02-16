package com.panda.salon_mgt_backend.models;

public enum BookingStatus {
    PENDING,      // booking created, not yet confirmed
    CONFIRMED,    // confirmed by system / staff
    CANCELLED,    // cancelled by user or staff
    COMPLETED,    // service delivered
    NO_SHOW       // customer did not arrive
}

