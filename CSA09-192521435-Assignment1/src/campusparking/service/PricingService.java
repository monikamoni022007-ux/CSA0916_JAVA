package campusparking.service;

import java.time.LocalDateTime;
import java.util.Objects;

import campusparking.model.Chargeable;
import campusparking.model.ParkingTicket;
import campusparking.model.Vehicle;

public final class PricingService {
    private PricingService() {}

    public static double calculateFee(ParkingTicket ticket, LocalDateTime exitTime, int chargedKWh) {
        Objects.requireNonNull(ticket, "Parking ticket is required");
        Objects.requireNonNull(exitTime, "Exit time is required");
        if (chargedKWh < 0) {
            throw new IllegalArgumentException("Energy usage cannot be negative");
        }

        LocalDateTime entryTime = ticket.getEntryTime();
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }

        Vehicle vehicle = ticket.getVehicle();
        double parkingFee = ticket.getUser().hasValidMonthlyPass(entryTime.toLocalDate())
            ? 0.0
            : vehicle.calculateFee(entryTime, exitTime);
        double chargingFee = vehicle instanceof Chargeable
            ? ((Chargeable) vehicle).chargingFee(chargedKWh)
            : 0.0;
        return parkingFee + chargingFee;
    }
}
