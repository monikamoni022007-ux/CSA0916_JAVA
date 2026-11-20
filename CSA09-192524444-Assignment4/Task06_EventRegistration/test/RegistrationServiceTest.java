import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class RegistrationServiceTest {
    public static void main(String[] args) throws Exception {
        RegistrationService service = new RegistrationService();

        check(!service.validateRegistrationNumber(null), "Null registration number should be invalid.");
        check(!service.validateRegistrationNumber("  "), "Blank registration number should be invalid.");
        check(service.validateRegistrationNumber("REG-1"), "Nonblank registration number should be valid.");
        check(!service.validateStudentName(null), "Null student name should be invalid.");
        check(!service.validateStudentName("  "), "Blank student name should be invalid.");
        check(service.validateStudentName("Ada"), "Nonblank student name should be valid.");

        expectValidationError(service, new Registration(0, " ", "Ada", 1, null, null),
                "Registration number is required.");
        expectValidationError(service, new Registration(0, "REG-1", " ", 1, null, null),
                "Student name is required.");
        expectValidationError(service, new Registration(0, "REG-1", "Ada", 0, null, null),
                "Please select an event.");

        Path dataDirectory = Files.createTempDirectory("task06-data-");
        Path dataFile = dataDirectory.resolve("registrations.properties");
        try {
            RegistrationService persistentService = new RegistrationService(new RegistrationDAO(dataFile));
            persistentService.registerStudent(new Registration(0, "REG-2", "Grace Hopper", 1, null, null));
            expectDuplicateError(persistentService,
                    new Registration(0, "REG-2", "Grace Hopper", 1, null, null));
            persistentService.registerStudent(new Registration(0, "REG-2", "Grace Hopper", 2, null, null));

            RegistrationService restartedService = new RegistrationService(new RegistrationDAO(dataFile));
            List<Registration> savedRecords = restartedService.loadRegistrations(0, 10);
            check(savedRecords.size() == 2, "Registrations should persist across service restarts.");
            check(savedRecords.get(0).getRegistrationId() == 1, "Registration IDs should be assigned and saved.");
            check("Campus Tech Expo".equals(savedRecords.get(0).getEventName()),
                    "Saved registrations should include the selected event.");
            check(restartedService.filterRegistrations("Robotics Workshop", 0, 10).size() == 1,
                    "Filtering should return only matching registrations.");
            check(restartedService.loadRegistrations(1, 1).size() == 1,
                    "Pagination should return the requested page.");
        } finally {
            Files.deleteIfExists(dataFile);
            Files.deleteIfExists(dataDirectory);
        }

        Path csv = Files.createTempFile("task06-registration-", ".csv");
        try {
            service.exportCsv(csv.toFile(), Arrays.asList(
                    new Registration(17, "REG,001", "Ada \"Lovelace\"\nSmith", 1, "Campus, Tech", "2026-11-15"),
                    new Registration(18, null, null, 2, null, null)));
            String lineEnding = System.lineSeparator();
            String expected = "\"registration_id\",\"registration_number\",\"student_name\",\"event_name\",\"registration_date\""
                    + lineEnding
                    + "\"17\",\"REG,001\",\"Ada \"\"Lovelace\"\"\nSmith\",\"Campus, Tech\",\"2026-11-15\""
                    + lineEnding
                    + "\"18\",\"\",\"\",\"\",\"\"" + lineEnding;
            String actual = Files.readString(csv, StandardCharsets.UTF_8);
            check(expected.equals(actual), "CSV should quote fields and escape delimiters, quotes, and nulls.");
        } finally {
            Files.deleteIfExists(csv);
        }

        System.out.println("Task 6 offline tests passed.");
    }

    private static void expectValidationError(RegistrationService service, Registration registration,
            String expectedMessage) throws Exception {
        try {
            service.registerStudent(registration);
        } catch (IllegalArgumentException ex) {
            check(expectedMessage.equals(ex.getMessage()), "Unexpected validation message: " + ex.getMessage());
            return;
        }
        throw new AssertionError("Expected validation error: " + expectedMessage);
    }

    private static void expectDuplicateError(RegistrationService service, Registration registration)
            throws Exception {
        try {
            service.registerStudent(registration);
        } catch (IllegalStateException ex) {
            check("Duplicate registration detected.".equals(ex.getMessage()),
                    "Unexpected duplicate message: " + ex.getMessage());
            return;
        }
        throw new AssertionError("Expected duplicate registration error.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
