package com.example.rental.ids;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongGenerator implements IdGenerator {
    private final AtomicLong seq = new AtomicLong(1L);

    @Override
    public Long nextId() {
        return seq.getAndIncrement();
    }
}
