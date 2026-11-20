# Task 06 – Event Registration Application

## Requirements
- Enter a registration number and student name.
- Select an event and register.
- Validate form inputs and prevent duplicate registration for the same event.
- Display and filter registrations, with pagination.
- Export the displayed event filter to CSV.
- Run storage operations with `SwingWorker`.

## Run without MySQL

Task 6 uses Java's built-in `Properties` file storage, so it runs with the JDK alone. Registrations are saved persistently to:

```text
%USERPROFILE%\JAVA_ASSIGNMENT_4\Task06_EventRegistration\registrations.properties
```

To use a different data file, set the `task06.dataFile` Java system property. No database connection is faked or attempted.

From the assignment root:

```powershell
javac -d out Task06_EventRegistration\src\*.java
java -cp out EventRegistrationApp
```

## Optional MySQL reference

`database/event_registration.sql` is retained as a schema/reference script. The application does not currently connect to MySQL; `database/config.example.properties` is not read by the application.

## Offline tests

```powershell
javac -d out Task06_EventRegistration\src\*.java Task06_EventRegistration\test\*.java
java -cp out RegistrationServiceTest
```

The tests use a temporary data file and verify validation, duplicate prevention, persistence, filtering, pagination, and CSV escaping without changing the user's registration data.
