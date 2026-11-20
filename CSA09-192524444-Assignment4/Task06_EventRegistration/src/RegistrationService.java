import java.io.File;
import java.io.IOException;
import java.util.List;

public class RegistrationService {
    private final RegistrationDAO registrationDAO;

    public RegistrationService() {
        this(new RegistrationDAO());
    }

    RegistrationService(RegistrationDAO registrationDAO) {
        this.registrationDAO = registrationDAO;
    }

    public boolean validateRegistrationNumber(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public boolean validateStudentName(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public List<Event> getEvents() {
        return registrationDAO.loadEvents();
    }

    public synchronized void registerStudent(Registration registration) throws IOException {
        if (!validateRegistrationNumber(registration.getRegistrationNumber())) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        if (!validateStudentName(registration.getStudentName())) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (registration.getEventId() <= 0) {
            throw new IllegalArgumentException("Please select an event.");
        }
        if (registrationDAO.isDuplicateRegistration(registration.getRegistrationNumber(), registration.getEventId())) {
            throw new IllegalStateException("Duplicate registration detected.");
        }
        registrationDAO.insertRegistration(registration);
    }

    public List<Registration> loadRegistrations(int offset, int limit) throws IOException {
        return registrationDAO.loadRegistrations(offset, limit);
    }

    public List<Registration> filterRegistrations(String eventName, int offset, int limit) throws IOException {
        if (eventName == null || eventName.equalsIgnoreCase("All Events")) {
            return registrationDAO.loadRegistrations(offset, limit);
        }
        return registrationDAO.filterRegistrations(eventName, offset, limit);
    }

    public void exportCsv(File file, List<Registration> records) throws IOException {
        registrationDAO.exportCsv(file, records);
    }
}
