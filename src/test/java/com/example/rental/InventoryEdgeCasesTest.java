package com.example.rental;

import com.example.rental.ids.AtomicLongGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryEdgeCasesTest {

    @Test
    void invalidRequestsAreRejected() {
        var svc = new ReservationService(new InMemoryInventory(Map.of(CarType.SEDAN, 1, CarType.SUV, 1, CarType.VAN, 1)), new AtomicLongGenerator());

        assertThrows(IllegalArgumentException.class, () -> new ReservationRequest(CarType.SEDAN, LocalDateTime.now(), 0L));

        assertThrows(IllegalArgumentException.class, () -> new Reservation(1L, CarType.SEDAN, LocalDateTime.now(), 0L));
    }

    @Test
    void removingReservationFreesCapacity() {
        var inv = new InMemoryInventory(Map.of(CarType.SEDAN, 1));
        var svc = new ReservationService(inv, new AtomicLongGenerator());
        var start = LocalDateTime.of(2025, 10, 21, 9, 0);

        var id = svc.reserveOrThrow(new ReservationRequest(CarType.SEDAN, start, 1L));
        assertEquals(1, inv.reservedCount(CarType.SEDAN, start.toLocalDate()));

        assertTrue(inv.removeReservationById(id));
        assertEquals(0, inv.reservedCount(CarType.SEDAN, start.toLocalDate()));

        assertTrue(svc.reserve(new ReservationRequest(CarType.SEDAN, start, 1L)).success());
    }

}