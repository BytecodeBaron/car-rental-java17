package com.example.rental;

public record ReservationResult(boolean success, Long reservationId, String message) {
    public static ReservationResult ok(Long id) {
        return new ReservationResult(true, id, "Reserved");
    }

    public static ReservationResult fail(String message) {
        return new ReservationResult(false, null, message);
    }
}