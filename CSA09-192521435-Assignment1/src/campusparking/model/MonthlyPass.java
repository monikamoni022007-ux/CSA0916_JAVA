package campusparking.model;

import java.time.LocalDate;
import java.util.Objects;

public final class MonthlyPass implements ParkingPass {
    private final LocalDate validFrom;
    private final LocalDate validThrough;

    public MonthlyPass(LocalDate validFrom, LocalDate validThrough) {
        this.validFrom = Objects.requireNonNull(validFrom, "Valid-from date is required");
        this.validThrough = Objects.requireNonNull(validThrough, "Valid-through date is required");
        if (validThrough.isBefore(validFrom)) {
            throw new IllegalArgumentException("Pass end date cannot be before its start date");
        }
    }

    @Override
    public boolean isValidOn(LocalDate date) {
        return date != null && !date.isBefore(validFrom) && !date.isAfter(validThrough);
    }
}
