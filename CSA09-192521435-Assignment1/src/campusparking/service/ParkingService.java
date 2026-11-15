package campusparking.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import campusparking.exception.InvalidParkingOperationException;
import campusparking.model.ParkingFloor;
import campusparking.model.ParkingTicket;
import campusparking.model.ParkingUser;
import campusparking.model.VisitorPass;
import campusparking.model.Vehicle;

public class ParkingService {
    private final ParkingFloor[] floors;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();

    public ParkingService(int[] slotsPerFloor) {
        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("At least one parking floor is required");
        }
        floors = new ParkingFloor[slotsPerFloor.length];
        for (int i = 0; i < slotsPerFloor.length; i++) {
            floors[i] = new ParkingFloor(i, slotsPerFloor[i]);
        }
    }

    public ParkingTicket enterVehicle(Vehicle vehicle, ParkingUser user, LocalDateTime entryTime) {
        if (vehicle == null || user == null || entryTime == null) {
            throw new InvalidParkingOperationException("Vehicle, user and entry time are required");
        }
        if (user.getParkingPass() instanceof VisitorPass
                && !user.hasValidVisitorPass(entryTime.toLocalDate())) {
            throw new InvalidParkingOperationException("Visitor pass has expired or is not valid on the entry date");
        }
        if (activeTickets.containsKey(vehicle.getRegNo())) {
            throw new InvalidParkingOperationException("Duplicate registration number: " + vehicle.getRegNo());
        }

        int[] location = findFirstFreeSlot();
        if (location == null) {
            throw new InvalidParkingOperationException("No parking slots available");
        }

        ParkingFloor floor = floors[location[0]];
        floor.allocate(vehicle.getRegNo(), location[1]);
        ParkingTicket ticket = new ParkingTicket(vehicle, user, entryTime, location[0], location[1]);
        activeTickets.put(vehicle.getRegNo(), ticket);
        return ticket;
    }

    public double exitVehicle(String regNo, LocalDateTime exitTime) {
        return exitVehicle(regNo, exitTime, 0);
    }

    public double exitVehicle(String regNo, LocalDateTime exitTime, int chargedKWh) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new InvalidParkingOperationException("Registration number is required");
        }
        if (exitTime == null) {
            throw new InvalidParkingOperationException("Exit time is required");
        }
        if (chargedKWh < 0) {
            throw new InvalidParkingOperationException("Energy usage cannot be negative");
        }
        String normalizedRegNo = regNo.trim().toUpperCase(Locale.ROOT);
        ParkingTicket ticket = activeTickets.get(normalizedRegNo);
        if (ticket == null) {
            throw new InvalidParkingOperationException("Vehicle " + regNo + " is not parked here");
        }
        double fee;
        try {
            fee = PricingService.calculateFee(ticket, exitTime, chargedKWh);
        } catch (IllegalArgumentException e) {
            throw new InvalidParkingOperationException(e.getMessage());
        }
        if (!floors[ticket.getFloor()].release(ticket.getSlot())) {
            throw new IllegalStateException("Ticket slot is unexpectedly free");
        }
        activeTickets.remove(ticket.getVehicle().getRegNo());
        return fee;
    }

    private int[] findFirstFreeSlot() {
        for (int floorIndex = 0; floorIndex < floors.length; floorIndex++) {
            int slotIndex = floors[floorIndex].findFirstFreeSlotIndex();
            if (slotIndex >= 0) {
                return new int[] { floorIndex, slotIndex };
            }
        }
        return null;
    }
}
