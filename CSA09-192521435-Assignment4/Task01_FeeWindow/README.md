# Task 01 – Fee Payment Window

## Diagnosis
The defective window has several design issues:
- Business logic mixed with Swing logic
- Incorrect `==` comparison for strings
- Missing validation for registration number and amount
- Database work on the EDT
- Blocking delays with `Thread.sleep()`
- Missing receipt and feedback
- SQL injection risk
- Weak resource handling

## Repair summary
A better design separates UI, validation, and database logic. The revised window uses a service class, validates input, handles exceptions, and processes payment in a `SwingWorker` to keep the interface responsive.
