package campusparking.model;

import java.time.LocalDate;
import java.util.Objects;

public final class VisitorPass implements ParkingPass {
    private final LocalDate issueDate;

    public VisitorPass(LocalDate issueDate) {
        this.issueDate = Objects.requireNonNull(issueDate, "Issue date is required");
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    @Override
    public boolean isValidOn(LocalDate date) {
        return date != null && date.equals(issueDate);
    }
}
