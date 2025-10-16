package com.example.rental;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface Inventory {
    static Iterable<LocalDate> enumerateDays(LocalDate startDate, long days) {
        if (days <= 0L) throw new IllegalArgumentException("days must be > 0");
        return () -> new java.util.Iterator<>() {
            private long i = 0L;

            @Override
            public boolean hasNext() {
                return i < days;
            }

            @Override
            public LocalDate next() {
                return startDate.plusDays(i++);
            }
        };
    }

    Map<CarType, Integer> capacity();

    void add(Reservation reservation);

    boolean removeReservationById(Long reservationId);

    List<Reservation> all();

    int reservedCount(CarType type, LocalDate onDate);
}
