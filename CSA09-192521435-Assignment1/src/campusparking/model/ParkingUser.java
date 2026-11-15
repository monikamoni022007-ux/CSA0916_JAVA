package campusparking.model;

import java.time.LocalDate;
import java.util.Objects;

public class ParkingUser {
    private final String userId;
    private final String name;
    private final UserRole role;
    private ParkingPass parkingPass;

    public ParkingUser(String userId, String name, UserRole role) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        this.userId = userId.trim();
        this.name = name.trim();
        this.role = Objects.requireNonNull(role, "User role is required");
    }

    public ParkingUser(String userId, String name, UserRole role, ParkingPass parkingPass) {
        this(userId, name, role);
        validatePassForRole(role, parkingPass);
        this.parkingPass = parkingPass;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean hasValidMonthlyPass(LocalDate date) {
        return (role == UserRole.STAFF || role == UserRole.STUDENT)
            && parkingPass instanceof MonthlyPass
            && parkingPass.isValidOn(date);
    }

    public boolean hasValidVisitorPass(LocalDate date) {
        return role == UserRole.VISITOR
            && parkingPass instanceof VisitorPass
            && parkingPass.isValidOn(date);
    }

    public void setParkingPass(ParkingPass parkingPass) {
        validatePassForRole(role, parkingPass);
        this.parkingPass = parkingPass;
    }

    private static void validatePassForRole(UserRole role, ParkingPass parkingPass) {
        if (parkingPass instanceof MonthlyPass
                && role != UserRole.STAFF && role != UserRole.STUDENT) {
            throw new IllegalArgumentException("Monthly passes are for staff or students");
        }
        if (parkingPass instanceof VisitorPass && role != UserRole.VISITOR) {
            throw new IllegalArgumentException("Visitor passes are for visitors");
        }
    }

    public ParkingPass getParkingPass() {
        return parkingPass;
    }

    public boolean hasPass() {
        return parkingPass != null;
    }
}
