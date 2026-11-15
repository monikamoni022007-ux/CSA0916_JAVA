package campusparking.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ParkingTicket {
    private static final AtomicLong TICKET_COUNTER = new AtomicLong(1);
    private final long ticketId;
    private final Vehicle vehicle;
    private final ParkingUser user;
    private final LocalDateTime entryTime;
    private final int floor;
    private final int slot;

    public ParkingTicket(Vehicle vehicle, ParkingUser user, LocalDateTime entryTime, int floor, int slot) {
        this.ticketId = TICKET_COUNTER.getAndIncrement();
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle is required");
        this.user = Objects.requireNonNull(user, "Parking user is required");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time is required");
        if (floor < 0 || slot < 0) {
            throw new IllegalArgumentException("Ticket floor and slot must be non-negative");
        }
        this.floor = floor;
        this.slot = slot;
    }

    public long getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingUser getUser() {
        return user;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return "Ticket #" + ticketId + " | " + vehicle.getRegNo() + " | floor=" + floor + " slot=" + slot + " | entry=" + entryTime;
    }
}
