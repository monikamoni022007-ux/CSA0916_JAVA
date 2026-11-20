# Design Documentation

## Swing and AWT layout decisions
- BorderLayout: used for the main area of large forms and result panels.
- FlowLayout: used for compact search and navigation controls.
- GridLayout: used for data-entry sections.
- CardLayout: suitable for switching between screens or panels if extended.

## Event-driven design
Each screen responds to user actions using listeners and validates input before updating the model or database.

## Program separation
Task 06 demonstrates the required layering:
- Presentation Layer: `EventRegistrationApp`, `RegistrationFrame`
- Service Layer: `RegistrationService`
- Data Access Layer: `RegistrationDAO`
- Model Layer: `Registration`, `Event`
- Database Layer: `DatabaseConnection`

## Validation and duplicate prevention
- Empty registration number is rejected.
- Empty student name is rejected.
- Missing event selection is rejected.
- Duplicate registrations are prevented by a unique database constraint.

## Main event flow
Window created -> UI initialized -> user input -> validation -> database operation -> result message -> close or continue.
