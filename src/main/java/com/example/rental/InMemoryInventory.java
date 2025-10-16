package com.example.rental;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInventory implements Inventory {
    private final Map<CarType, Integer> capacity;
    private final Map<Long, Reservation> byId = new ConcurrentHashMap<>();
    private final Map<CarType, Map<LocalDate, Integer>> calendar = new EnumMap<>(CarType.class);

    public InMemoryInventory(Map<CarType, Integer> capacity) {
        if (capacity == null || capacity.isEmpty()) throw new IllegalArgumentException("capacity required");
        this.capacity = Map.copyOf(capacity);
        for (CarType t : CarType.values()) calendar.put(t, new ConcurrentHashMap<>());
    }

    @Override
    public Map<CarType, Integer> capacity() {
        return capacity;
    }

    @Override
    public void add(Reservation reservation) {
        byId.put(reservation.getId(), reservation);
        for (LocalDate d : Inventory.enumerateDays(reservation.getStart().toLocalDate(), reservation.getDays())) {
            calendar.get(reservation.getType()).merge(d, 1, Integer::sum);
        }
    }

    @Override
    public boolean removeReservationById(Long reservationId) {
        Reservation r = byId.remove(reservationId);
        if (r == null) return false;
        for (LocalDate d : Inventory.enumerateDays(r.getStart().toLocalDate(), r.getDays())) {
            calendar.get(r.getType()).merge(d, -1, Integer::sum);
        }
        return true;
    }

    @Override
    public List<Reservation> all() {
        return List.copyOf(byId.values());
    }

    @Override
    public int reservedCount(CarType type, LocalDate onDate) {
        return calendar.get(type).getOrDefault(onDate, 0);
    }

    public boolean hasAvailability(CarType type, LocalDateTime start, long days) {
        int cap = capacity.getOrDefault(type, 0);
        for (LocalDate d : Inventory.enumerateDays(start.toLocalDate(), days)) {
            if (reservedCount(type, d) >= cap) return false;
        }
        return true;
    }
}
