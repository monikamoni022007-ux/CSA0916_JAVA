# JAVA ASSIGNMENT 4 – Event-Driven GUI Design

This repository contains a Java Swing assignment covering six task areas: fee payment diagnostics, event-driven appointment design, library search layouts, draw pad event handling, currency conversion listener styles, and a persistent event registration application.

## Project structure

- Task01_FeeWindow/
- Task02_AppointmentDesign/
- Task03_LibrarySearch/
- Task04_DrawPad/
- Task05_CurrencyConverter/
- Task06_EventRegistration/
- documentation/
- output_screenshots/

## Java version
- OpenJDK 21 verified in environment

## Technical stack
- Java Swing / AWT
- Java Properties-based local persistence (Task 6)
- Java standard library
- SwingWorker for background work

## Quick compile
```powershell
cd "C:\Users\KAVYA SREE\Documents\JAVA 4\JAVA_ASSIGNMENT_4"
mkdir out
javac -d out Task01_FeeWindow\src\*.java
javac -d out Task03_LibrarySearch\src\*.java
javac -d out Task04_DrawPad\src\*.java
javac -d out Task05_CurrencyConverter\src\*.java
javac -d out Task06_EventRegistration\src\*.java
```

## Task 6 storage
Task 6 runs with the JDK alone and saves registrations to `%USERPROFILE%\JAVA_ASSIGNMENT_4\Task06_EventRegistration\registrations.properties`. The SQL file under `Task06_EventRegistration/database/` is retained as an optional schema reference; MySQL is not required.

## Execution
```powershell
java -cp out FeeWindow
java -cp out AppointmentDeskDemo
java -cp out LibrarySearchApp
java -cp out DrawPad
java -cp out Converter
java -cp out EventRegistrationApp
```

See `Task06_EventRegistration/README.md` for Task 6-specific compile, run, and test commands.

## Screenshot evidence
All required screenshot evidence should be collected under:
- output_screenshots/Task01/
- output_screenshots/Task02/
- output_screenshots/Task03/
- output_screenshots/Task04/
- output_screenshots/Task05/
- output_screenshots/Task06/

## GitHub note
This repository is prepared for local Git commits and GitHub submission, but no remote URL is configured yet.
