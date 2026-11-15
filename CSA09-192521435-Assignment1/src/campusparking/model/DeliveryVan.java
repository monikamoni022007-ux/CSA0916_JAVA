package campusparking.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class DeliveryVan extends Vehicle {
    public static final double PEAK_RATE = 60.0;
    public static final double OFF_PEAK_RATE = 30.0;

    public DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative");
        }
        if (minutes == 0) {
            return 0.0;
        }
        throw new IllegalArgumentException("Delivery van pricing requires entry and exit times");
    }

    @Override
    public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
        Objects.requireNonNull(entryTime, "Entry time is required");
        Objects.requireNonNull(exitTime, "Exit time is required");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        if (exitTime.equals(entryTime)) {
            return 0.0;
        }

        double peakSeconds = 0.0;
        LocalDate date = entryTime.toLocalDate();
        LocalDate finalDate = exitTime.toLocalDate();
        while (!date.isAfter(finalDate)) {
            LocalDateTime peakStart = date.atTime(9, 0);
            LocalDateTime peakEnd = date.atTime(17, 0);
            LocalDateTime overlapStart = entryTime.isAfter(peakStart) ? entryTime : peakStart;
            LocalDateTime overlapEnd = exitTime.isBefore(peakEnd) ? exitTime : peakEnd;
            if (overlapEnd.isAfter(overlapStart)) {
                peakSeconds += Duration.between(overlapStart, overlapEnd).toNanos() / 1_000_000_000.0;
            }
            date = date.plusDays(1);
        }

        double totalSeconds = Duration.between(entryTime, exitTime).toNanos() / 1_000_000_000.0;
        double offPeakSeconds = totalSeconds - peakSeconds;
        return (peakSeconds * PEAK_RATE + offPeakSeconds * OFF_PEAK_RATE) / 3600.0;
    }

    @Override
    protected double ratePerHour() {
        return OFF_PEAK_RATE;
    }
}
