package campusparking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || !regNo.trim().matches("[A-Za-z0-9-]+")) {
            throw new IllegalArgumentException("Registration number must contain letters, digits, or hyphens");
        }
        this.regNo = regNo.trim().toUpperCase(Locale.ROOT);
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double getRatePerHour() {
        return ratePerHour();
    }

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative");
        }
        if (minutes == 0) {
            return 0.0;
        }
        int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
        return hours * ratePerHour();
    }

    public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
        Objects.requireNonNull(entryTime, "Entry time is required");
        Objects.requireNonNull(exitTime, "Exit time is required");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Parking duration is too long");
        }
        return calculateFee((int) minutes);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{regNo='" + regNo + "'}";
    }
}
