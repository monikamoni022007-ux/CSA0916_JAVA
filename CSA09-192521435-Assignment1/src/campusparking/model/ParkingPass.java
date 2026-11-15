package campusparking.model;

import java.time.LocalDate;

public interface ParkingPass {
    boolean isValidOn(LocalDate date);
}
