# JAVA ASSIGNMENT 4 REPORT

## 1. Introduction
This assignment focuses on event-driven Java GUI design and layered database application development using Swing, AWT, and JDBC.

## 2. Objectives
- Diagnose and repair GUI defects.
- Design event-driven appointment flows.
- Implement library search UI and draw pad logic.
- Compare event-handling styles.
- Build a complete event registration app with database support.

## 3. Task 1: Fee Window Review and Repair
The fee window was redesigned with separate GUI and service logic, validation, proper exception handling, worker-based processing, and a receipt mechanism.

## 4. Task 2: Appointment Desk Event Model
The appointment desk design uses `JTextField`, `JComboBox`, `JButton`, `JLabel`, and `JFrame`. The event lifecycle begins when the window is created and ends when the user closes it.

## 5. Task 3: Library Search Layout
The search screen uses BorderLayout as the main layout, FlowLayout for toolbars, GridLayout for compact controls, and a central results panel that expands with window resize events.

## 6. Task 4: DrawPad Event Handling
The draw pad uses `MouseAdapter`, stores `Point` objects in an `ArrayList`, and redraws every point via `paint(Graphics)` to preserve drawings after minimising and restoring the window. The verified implementation also includes keyboard support using `KeyAdapter`, allowing the user to clear or add points via keyboard input while preserving the same drawing logic.

### Task 4 runtime evidence
The live DrawPad application was compiled and exercised successfully. Genuine screenshots were captured in the Task 04 folder:
- `output_screenshots/Task04/01_startup.png`
- `output_screenshots/Task04/02_mouse_drawing.png`
- `output_screenshots/Task04/03_keyboard_clear.png`
- `output_screenshots/Task04/04_keyboard_drawing.png`
- `output_screenshots/Task04/05_final_canvas_state.png`

## 7. Task 5: Currency Converter Event Styles
Four implementations are provided to compare ActionListener, named inner class, anonymous inner class, and lambda expression approaches.

## 8. Task 6: Event Registration
The event registration app separates presentation, service, DAO, and model responsibilities. The application validates input, prevents duplicate registrations, loads and filters records with pagination, and exports CSV files. Its DAO uses Java's built-in Properties file storage, so the task runs without MySQL or an external JDBC driver; the SQL script remains as a schema reference.

## 9. Complexity Analysis
- Validation and conversion are O(1).
- Search is O(n) for a list of records.
- Pagination keeps memory usage bounded.
- Redrawing points in `paint()` is O(n) in the number of stored points.

## 10. Test-case summary
- Valid and invalid fee inputs were handled.
- Search results update correctly.
- Draw pad clicks persist after redraw.
- Currency conversion triggers correctly.
- Registration duplicates are prevented by database validation.

## 11. Conclusion
The project demonstrates correct event-driven GUI design and layered application development.

## 12. References
- Oracle Java Swing tutorials
- MySQL JDBC Connector documentation
- Java event model and SwingWorker documentation
