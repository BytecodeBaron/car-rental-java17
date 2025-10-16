# Car Rental (Java 17 + Gradle Kotlin DSL)

A small, test-driven car rental simulation. Focus: reserve a **car type** (Sedan/SUV/Van) for **N days** starting at a date/time; enforce **limited fleet capacity per day**; prevent overbooking.

## Quick start

```bash
# Java 17 required
./gradlew clean test
```

All tests should pass (JUnit 5).

---

## Why this project?

- **OOP design:** small, focused classes; storage abstracted behind `Inventory`.
- **Day precision:** business rule is “book **N** days” → we enumerate exact dates.
- **Thread-aware:** in-memory concurrency via `ConcurrentHashMap`.
- **Tests first:** overlapping reservations, capacity, back-to-back, invalid inputs.

---

## Project structure

```
src/
  main/java/com/example/rental/
    CarType.java
    Reservation.java              # stores long days; end = start + days (exclusive)
    ReservationRequest.java       # days: long
    ReservationResult.java        # id: Long
    Inventory.java                # interface + day enumerator
    InMemoryInventory.java        # per-type, per-day counters; ConcurrentHashMap
    ReservationService.java       # orchestration + Id generation
    exceptions/OverbookingException.java
    ids/
      IdGenerator.java
      AtomicLongGenerator.java
  test/java/com/example/rental/
    ReservationServiceTest.java
    InventoryEdgeCasesTest.java
build.gradle.kts
settings.gradle.kts
```

---

## How it works

### Core rule
A reservation covers **exactly N calendar days** starting from `start`:
- `Reservation` stores `long days` and derives `end = start.plusDays(days)` (exclusive).
- Availability checks iterate those **N dates** and ensure used `< capacity`.

### Data model (no per-vehicle IDs yet)
- Capacity is tracked **per car type** (SEDAN/SUV/VAN), not per physical car.
- We maintain a **per-day counter** for each type:

```java
Map<CarType, Map<LocalDate, Integer>> calendar;
```

When a reservation is added, we increment that counter for each covered date:
```java
for (LocalDate d : Inventory.enumerateDays(reservation.getStart().toLocalDate(), reservation.getDays())) {
    calendar.get(reservation.getType()).merge(d, 1, Integer::sum);
}
```
- `enumerateDays(startDate, days)` yields the exact days: start, start+1, …, start+(days-1)
- `merge(d, 1, Integer::sum)` means “insert 1 if missing; otherwise add 1”

### Why `ConcurrentHashMap`?
- Safe under concurrent reads/writes (e.g., parallel tests or future REST endpoints).
- `merge` is atomic; iterators are weakly consistent (no `ConcurrentModificationException`).
- Not a transaction boundary—multi-step updates aren’t atomic across **both** maps; for that, you’d add a lock or use a DB.

---

## Public API (example)

```java
import com.example.rental.*;
import com.example.rental.ids.AtomicLongGenerator;
import java.time.LocalDateTime;
import java.util.Map;

var inventory = new InMemoryInventory(Map.of(
    CarType.SEDAN, 2,
    CarType.SUV,   1,
    CarType.VAN,   1
));
var service = new ReservationService(inventory, new AtomicLongGenerator());

var start = LocalDateTime.of(2025, 10, 20, 10, 0);
var res   = service.reserve(new ReservationRequest(CarType.SEDAN, start, 3L));

if (res.success()) {
    System.out.println("Reserved with ID: " + res.reservationId());
} else {
    System.out.println("Failed: " + res.message());
}
```

---

## Tests (what they prove)

- **`ReservationServiceTest`**
    - Reserves when capacity is available.
    - Prevents overlapping overbooking (single-SUV capacity).
    - Allows back-to-back (end of day → next day start).
    - Enforces **daily** granularity (multiple bookings same day up to capacity).

- **`InventoryEdgeCasesTest`**
    - Rejects invalid requests (zero/negative days).
    - Removing a reservation decrements counters and frees capacity.

Run selective tests:
```bash
./gradlew test --tests "*ReservationServiceTest"
./gradlew test --tests "*InventoryEdgeCasesTest"
```

---

## Design choices (and trade-offs)

- **IDs as `Long`**  
  Simple, compact, readable. For public APIs consider opaque IDs later (ULID/UUIDv7) to prevent enumeration.

- **Inventory as an interface**  
  Enables swapping the backend (in-memory now; JDBC/JPA later) and eases testing.

- **Daily capacity, not per-hour**  
  Keeps logic tight and matches common rental policy. If hourly precision is needed, switch to interval math per vehicle.

---

## What’s missing (on purpose)

- No REST API, DB, or pricing.
- No per-vehicle assignment (fleet is abstract by type).
- No timezone/DST complexity (uses `LocalDateTime` only).

**Next steps** (if extended):
- Add REST (Spring Boot) + persistence (PostgreSQL/JPA).
- Per-vehicle allocation & same-hour handoff rules.
- Pricing, cancellations/modifications, and more test scenarios.

---

## License

MIT