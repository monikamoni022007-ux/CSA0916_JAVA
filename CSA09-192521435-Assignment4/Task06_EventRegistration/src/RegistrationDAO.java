import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RegistrationDAO {
    private static final List<Event> EVENTS = Arrays.asList(
            new Event(1, "Campus Tech Expo", "2026-12-12", "Main Auditorium"),
            new Event(2, "Robotics Workshop", "2026-12-15", "Innovation Lab"),
            new Event(3, "Design Thinking Hackathon", "2026-12-20", "Innovation Hall"));
    private final Path storageFile;

    public RegistrationDAO() {
        this(defaultStorageFile());
    }

    RegistrationDAO(Path storageFile) {
        this.storageFile = storageFile;
    }

    private static Path defaultStorageFile() {
        String configuredPath = System.getProperty("task06.dataFile");
        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            return Path.of(configuredPath);
        }
        return Path.of(System.getProperty("user.home"), "JAVA_ASSIGNMENT_4",
                "Task06_EventRegistration", "registrations.properties");
    }

    public List<Event> loadEvents() {
        return new ArrayList<>(EVENTS);
    }

    public synchronized void insertRegistration(Registration registration) throws IOException {
        if (findEvent(registration.getEventId()) == null) {
            throw new IllegalArgumentException("Please select a valid event.");
        }
        List<Registration> registrations = readRegistrations();
        if (containsRegistration(registrations, registration.getRegistrationNumber(), registration.getEventId())) {
            throw new IllegalStateException("Duplicate registration detected.");
        }

        Properties properties = loadProperties();
        int nextId = readInteger(properties, "nextId", 1);
        Registration savedRegistration = new Registration(nextId, registration.getRegistrationNumber(),
                registration.getStudentName(), registration.getEventId(),
                findEvent(registration.getEventId()).getEventName(), LocalDateTime.now().toString());
        registrations.add(savedRegistration);
        writeRegistrations(properties, registrations, nextId + 1);
        saveProperties(properties);
    }

    public synchronized List<Registration> loadRegistrations(int offset, int limit) throws IOException {
        return page(readRegistrations(), offset, limit);
    }

    public synchronized List<Registration> filterRegistrations(String eventName, int offset, int limit)
            throws IOException {
        List<Registration> registrations = readRegistrations();
        if (eventName != null && !eventName.trim().isEmpty()) {
            registrations.removeIf(registration -> !eventName.equals(registration.getEventName()));
        }
        return page(registrations, offset, limit);
    }

    public synchronized boolean isDuplicateRegistration(String registrationNumber, int eventId) throws IOException {
        return containsRegistration(readRegistrations(), registrationNumber, eventId);
    }

    public void exportCsv(java.io.File file, List<Registration> registrations) throws IOException {
        try (java.io.BufferedWriter writer = Files.newBufferedWriter(file.toPath(),
                java.nio.charset.StandardCharsets.UTF_8)) {
            writeCsvRow(writer, "registration_id", "registration_number", "student_name", "event_name",
                    "registration_date");
            for (Registration registration : registrations) {
                writeCsvRow(writer, Integer.toString(registration.getRegistrationId()),
                        registration.getRegistrationNumber(), registration.getStudentName(),
                        registration.getEventName(), registration.getRegistrationDate());
            }
        }
    }

    private List<Registration> readRegistrations() throws IOException {
        Properties properties = loadProperties();
        int count = readInteger(properties, "registration.count", 0);
        if (count < 0) {
            throw new IOException("Invalid registration count in " + storageFile);
        }

        List<Registration> registrations = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String prefix = "registration." + index + ".";
            int registrationId = readInteger(properties, prefix + "id", -1);
            int eventId = readInteger(properties, prefix + "eventId", -1);
            Event event = findEvent(eventId);
            if (registrationId < 1 || event == null) {
                throw new IOException("Invalid registration data in " + storageFile + " at record " + index);
            }
            registrations.add(new Registration(registrationId,
                    requireProperty(properties, prefix + "registrationNumber"),
                    requireProperty(properties, prefix + "studentName"), eventId, event.getEventName(),
                    requireProperty(properties, prefix + "registrationDate")));
        }
        return registrations;
    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        if (Files.exists(storageFile)) {
            try (InputStream input = Files.newInputStream(storageFile)) {
                properties.load(input);
            }
        }
        return properties;
    }

    private void writeRegistrations(Properties properties, List<Registration> registrations, int nextId) {
        properties.setProperty("nextId", Integer.toString(nextId));
        properties.setProperty("registration.count", Integer.toString(registrations.size()));
        for (int index = 0; index < registrations.size(); index++) {
            Registration registration = registrations.get(index);
            String prefix = "registration." + index + ".";
            properties.setProperty(prefix + "id", Integer.toString(registration.getRegistrationId()));
            properties.setProperty(prefix + "registrationNumber", registration.getRegistrationNumber());
            properties.setProperty(prefix + "studentName", registration.getStudentName());
            properties.setProperty(prefix + "eventId", Integer.toString(registration.getEventId()));
            properties.setProperty(prefix + "registrationDate", registration.getRegistrationDate());
        }
    }

    private void saveProperties(Properties properties) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "registrations-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                properties.store(output, "Task 6 event registrations");
            }
            try {
                Files.move(temporaryFile, storageFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, storageFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String requireProperty(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IOException("Missing " + key + " in " + storageFile);
        }
        return value;
    }

    private int readInteger(Properties properties, String key, int defaultValue) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid " + key + " in " + storageFile, ex);
        }
    }

    private Event findEvent(int eventId) {
        for (Event event : EVENTS) {
            if (event.getEventId() == eventId) {
                return event;
            }
        }
        return null;
    }

    private boolean containsRegistration(List<Registration> registrations, String registrationNumber, int eventId) {
        for (Registration registration : registrations) {
            if (registration.getEventId() == eventId
                    && registration.getRegistrationNumber().equals(registrationNumber)) {
                return true;
            }
        }
        return false;
    }

    private List<Registration> page(List<Registration> registrations, int offset, int limit) {
        if (offset < 0 || limit < 0) {
            throw new IllegalArgumentException("Offset and limit must not be negative.");
        }
        if (offset >= registrations.size() || limit == 0) {
            return new ArrayList<>();
        }
        int end = (int) Math.min((long) registrations.size(), (long) offset + limit);
        return new ArrayList<>(registrations.subList(offset, end));
    }

    private void writeCsvRow(java.io.BufferedWriter writer, String... values) throws IOException {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write('"');
            if (values[index] != null) {
                writer.write(values[index].replace("\"", "\"\""));
            }
            writer.write('"');
        }
        writer.newLine();
    }
}
