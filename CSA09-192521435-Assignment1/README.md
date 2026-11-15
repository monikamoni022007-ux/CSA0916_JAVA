# Campus Smart-Parking System — Java Assignment 1

**Course:** CSA09 – Java Programming
**Student name:** Kavya Sree T
**College register number:** 192524444
**JDK:** OpenJDK 21
**GitHub repository/link:** Pending repository creation and confirmed remote

## Project

This Java SE project implements the 12 tasks for the campus smart-parking assignment. The integrated model supports vehicle entry and exit, tickets, unequal floor capacities, duplicate and invalid-operation checks, monthly passes, EV energy billing, configurable e-bike flat pricing, delivery-van peak pricing and one-day visitor pass validity.

The official PDF does not give the e-bike flat-fee amount. Supply the course-specified amount when constructing an `EBike`; no default tariff is assumed. By user decision, the visitor pass validates one-day access but does not waive the visitor's normal parking fee.

## Structure

- `Task01`–`Task12`: task-specific examples and demonstrations
- `src/campusparking/model`: vehicles, tickets, users, passes and floors
- `src/campusparking/service`: entry/exit and pricing coordination
- `src/campusparking/app`: runnable application and executable checks
- `documentation`: requirements, tracker, report, test results and screenshot checklist
- `output_screenshots/Task01`–`Task12`: prepared for genuine screenshots

## Compile all tasks and integrated sources (PowerShell)

```powershell
cd "C:\Users\KAVYA SREE\JAVA 1"
$javaFiles = Get-ChildItem -Recurse -Filter *.java |
    Where-Object { $_.FullName -notmatch '\\(out|build)\\' } |
    Select-Object -ExpandProperty FullName
javac -Xlint:all -d out $javaFiles
```

## Run

```powershell
java -cp out campusparking.app.Main
java -cp out task03.ParkingFacility
java -cp out task08.FeeDemo
java -cp out task11.SmartParkingSystemDemo
java -cp out task12.Day4DesignChallenge
```

Run all integrated checks:

```powershell
java -cp out campusparking.app.AcceptanceTests
```

Run the educational lookup timing comparison:

```powershell
java -cp out campusparking.app.LookupBenchmark
```

Other individual task entry points are listed in each task folder's README.

## OOP concepts

The implementation demonstrates encapsulation, constructors/chaining, static ticket IDs, inheritance, overriding, overloading, runtime polymorphism, abstract classes, capability/pass interfaces and packages. Design alternatives, pseudocode, complexity and UML are in [documentation/IMPLEMENTATION_REPORT.md](documentation/IMPLEMENTATION_REPORT.md).

## Verification and submission

Actual build and test output is recorded in [documentation/TEST_REPORT.md](documentation/TEST_REPORT.md). No screenshots or GitHub link are claimed until they are genuinely captured/configured. See [documentation/SCREENSHOT_CHECKLIST.md](documentation/SCREENSHOT_CHECKLIST.md) for capture steps. The local Git repository currently has one initial commit; the required task-by-task progressive history and GitHub remote/link still need to be prepared without falsely representing the earlier implementation sequence.
