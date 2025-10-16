package com.example.rental;

import com.example.rental.exceptions.OverbookingException;
import com.example.rental.ids.IdGenerator;

public class ReservationService {
    private final InMemoryInventory inventory;
    private final IdGenerator idGen;

    public ReservationService(InMemoryInventory inventory, IdGenerator idGen) {
        this.inventory = inventory;
        this.idGen = idGen;
    }

    public ReservationResult reserve(ReservationRequest request) {
        if (!inventory.hasAvailability(request.type(), request.start(), request.days())) {
            return ReservationResult.fail("No availability for " + request.type() + " in requested period.");
        }
        Long id = idGen.nextId();
        Reservation r = new Reservation(id, request.type(), request.start(), request.days());
        inventory.add(r);
        return ReservationResult.ok(id);
    }

    public Long reserveOrThrow(ReservationRequest request) {
        if (!inventory.hasAvailability(request.type(), request.start(), request.days())) {
            throw new OverbookingException("Overbooking prevented for " + request.type());
        }
        Long id = idGen.nextId();
        Reservation r = new Reservation(id, request.type(), request.start(), request.days());
        inventory.add(r);
        return id;
    }

    public InMemoryInventory getInventory() {
        return inventory;
    }
}