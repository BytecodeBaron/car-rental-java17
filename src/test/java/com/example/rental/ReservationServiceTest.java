package com.example.rental;

import com.example.rental.ids.AtomicLongGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationService newService() {
        var capacity = Map.of(CarType.SEDAN, 2, CarType.SUV, 1, CarType.VAN, 1);
        return new ReservationService(new InMemoryInventory(capacity), new AtomicLongGenerator());
    }

    @Test
    void reservesWhenCapacityAvailable() {
        var svc = newService();
        var start = LocalDateTime.of(2025, 10, 20, 10, 0);
        var result = svc.reserve(new ReservationRequest(CarType.SEDAN, start, 3L));
        assertTrue(result.success());
        assertNotNull(result.reservationId());
        assertTrue(result.reservationId() > 0);
    }

    @Test
    void preventsOverbookingAcrossDays() {
        var svc = newService();
        var start = LocalDateTime.of(2025, 10, 20, 10, 0);
        assertTrue(svc.reserve(new ReservationRequest(CarType.SUV, start, 2L)).success());
        var r2 = svc.reserve(new ReservationRequest(CarType.SUV, start.plusHours(1), 1L));
        assertFalse(r2.success());
    }

    @Test
    void allowsBackToBackReservationSameDayBoundary() {
        var svc = newService();
        var start = LocalDateTime.of(2025, 10, 20, 10, 0);

        var r1 = svc.reserve(new ReservationRequest(CarType.VAN, start, 1L));
        assertTrue(r1.success());

        var r2 = svc.reserve(new ReservationRequest(CarType.VAN, start.plusDays(1), 1L));
        assertTrue(r2.success());
    }

    @Test
    void usesDailyGranularity() {
        var svc = newService();
        var start = LocalDateTime.of(2025, 10, 20, 8, 0);
        assertTrue(svc.reserve(new ReservationRequest(CarType.SEDAN, start, 1L)).success());

        var r2 = svc.reserve(new ReservationRequest(CarType.SEDAN, start.plusHours(6), 1L));
        assertTrue(r2.success(), "Capacity is 2, so second booking same day should succeed");

        var r3 = svc.reserve(new ReservationRequest(CarType.SEDAN, start.plusHours(12), 1L));
        assertFalse(r3.success(), "Third booking on same day should fail (capacity=2)");
    }
}