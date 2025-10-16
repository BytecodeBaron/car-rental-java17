package com.example.rental;

import java.time.LocalDateTime;
import java.util.Objects;

public record ReservationRequest(CarType type, LocalDateTime start, long days) {
    public ReservationRequest {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(start, "start");
        if (days <= 0L) throw new IllegalArgumentException("days must be > 0");
    }
}
