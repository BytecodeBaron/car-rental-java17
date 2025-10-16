package com.example.rental;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final Long id;
    private final CarType type;
    private final LocalDateTime start;
    private final long days;
    private final LocalDateTime end;

    public Reservation(Long id, CarType type, LocalDateTime start, long days) {

        if (id == null) throw new IllegalArgumentException("id required");
        if (type == null) throw new IllegalArgumentException("type required");
        if (start == null) throw new IllegalArgumentException("start required");
        if (days <= 0L) throw new IllegalArgumentException("days must be > 0");

        this.id = id;
        this.type = type;
        this.start = start;
        this.days = days;
        this.end = start.plusDays(days);
    }

    public Long getId() {
        return id;
    }

    public CarType getType() {
        return type;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public long getDays() {
        return days;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
