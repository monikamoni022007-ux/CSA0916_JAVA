# Viva Preparation Guide

## Key questions to answer
- Why should database work not run on the EDT?
- Why is `==` wrong for string comparison?
- Why is `Thread.sleep()` a poor choice for GUI responsiveness?
- What is the purpose of `SwingWorker`?
- Why do we need `PreparedStatement`?
- Why does `getGraphics()` not preserve drawings?
- Why does a unique database constraint help with duplicates?
- What is the Event Dispatch Thread (EDT)?
- Why are layout managers important in Swing?
- Why is model-view separation useful in GUI design?
- What is the difference between event listeners and event handlers?
- Why does the Main Thread not suit long database operations?

## Short answers
1. The EDT handles repainting and events; blocking it makes the UI freeze.
2. Strings must be compared using `.equals()` because `==` compares object references, not content.
3. `Thread.sleep()` pauses the UI thread and can freeze the interface; `SwingWorker` keeps Java Swing responsive while background work runs.
4. `SwingWorker` runs time-consuming tasks in a background thread and updates the UI safely on the EDT.
5. `PreparedStatement` prevents SQL injection, improves readability, and reuses parameterized queries safely.
6. `getGraphics()` paints only to the current display surface; the drawing is not stored in a persistent model, so it disappears on repaint.
7. A unique database constraint prevents duplicate records and keeps data consistent when users submit the same data twice.
8. The EDT is the single thread responsible for all Swing UI changes, repainting, and event processing.
9. Layout managers make interfaces responsive and consistent across screen sizes and resolutions.
10. Model-view separation makes code easier to maintain, test, and extend because UI code is separated from data logic.
11. Event listeners register interest in an event; an event handler is the method that responds when the event occurs.
12. Long database work should not block the EDT; it should be done in the background and then synchronized back to the UI.

## Expanded viva explanations

### 1) Why not do database work on the EDT?
The Swing Event Dispatch Thread is responsible for repainting and processing user actions. If a database call or a long query is executed there, the UI becomes unresponsive and the user cannot click buttons or even close the window. The correct approach is to offload the database work to a background thread and update the GUI only after the result arrives.

### 2) Why is `==` wrong for string comparison?
`==` checks whether two references point to the same object in memory. Two separate String objects can have the same text but still compare unequal with `==`. For meaningful text comparison we must use `.equals()`, and for case-insensitive comparison `.equalsIgnoreCase()`.

### 3) Why is `Thread.sleep()` a poor choice for GUI responsiveness?
`Thread.sleep()` pauses the current thread. If it is used on the EDT, the entire interface stops responding temporarily. This is especially bad in Swing because the thread responsible for interaction and painting is blocked, resulting in a frozen window.

### 4) Purpose of `SwingWorker`
`SwingWorker` is designed for long-running tasks in Swing applications. It runs background tasks in a worker thread and then delivers results to the UI thread when finished. This keeps the application responsive while performing data access, calculations, file operations, or network tasks.

### 5) Why use `PreparedStatement`?
`PreparedStatement` protects against SQL injection by treating user input as data, not executable SQL. It also lets us pass values safely and maintain cleaner, reusable SQL templates. This is especially important in registration, login, and search modules.

### 6) Why does `getGraphics()` not preserve drawings?
`getGraphics()` returns a temporary graphics context for the current component. It does not permanently store the drawing. When the component is repainted, the previous drawing is lost because it was never part of the component's persistent state. The proper approach is to store drawing data in an object model and redraw it in `paintComponent()`.

### 7) Why do unique constraints help with duplicates?
A unique constraint ensures that the same value cannot be inserted more than once in a specific field or combination of fields. For example, a unique email or registration ID prevents duplicate submissions. This improves correctness and avoids inconsistent database rows.

### 8) What is the EDT?
The Event Dispatch Thread is the thread that dispatches user events such as mouse clicks, key presses, and repaint requests. Swing is not thread-safe, so all changes to components should be done on the EDT to avoid race conditions and UI glitches.

### 9) Why are layout managers important?
Swing layout managers organize components dynamically based on the container size, platform, and resolution. They help build responsive interfaces without hardcoding pixel positions. Common managers include `BorderLayout`, `FlowLayout`, `GridLayout`, and `GridBagLayout`.

### 10) Why model-view separation matters
Separating the data model from the user interface makes the application more maintainable. UI code can change without altering the business logic, and vice versa. It also makes debugging easier because each layer has a clear responsibility.

### 11) Event listeners vs handlers
An event listener is an object registered to listen for a specific kind of event. An event handler is the callback method executed when that event occurs, such as `actionPerformed()`, `mouseClicked()`, or `itemStateChanged()`.

### 12) Why should database tasks be backgrounded?
Database tasks often involve network latency or disk I/O. Running them in the main thread blocks the UI and defeats the purpose of event-driven programming. By using `SwingWorker` or a background thread, the UI remains interactive while database work completes.

## Assignment-specific viva questions

### Task 1: FeeWindow / event-driven diagnostics
- What is the purpose of the fee window application?
- Why is event handling necessary for validation and diagnostic logic?
- How do buttons, text fields, and listeners work together in Swing?
- Why should you avoid performing long processes on the UI thread?

Expected answer: The fee window demonstrates how GUI components respond to events and how data validation or calculations can be triggered in response to user actions.

### Task 2: AppointmentDesign
- What is the advantage of modeling appointments as objects?
- Why is event-driven design useful for scheduling systems?
- How does a GUI separation between form input and appointment logic help?

Expected answer: It organizes code into clear responsibilities and allows user actions to update state in a structured way.

### Task 3: LibrarySearch
- Why are layout managers used in search interfaces?
- How do text fields and buttons interact with search logic?
- What is the benefit of separating search input from result display?

Expected answer: Layout managers make the interface flexible and clean, while event-based actions connect user input to search results.

### Task 4: DrawPad
- Why must we redraw in `paintComponent()` instead of only drawing once?
- How do mouse events allow freehand drawing?
- Why does repainting matter when the window resizes or redraws?

Expected answer: Drawings are stateful and must be redrawn on each repaint to remain visible and consistent.

### Task 5: CurrencyConverter
- What is the difference between using listeners and lambda expressions?
- Why are listeners important for text changes and button actions?
- What does event-driven conversion mean in this context?

Expected answer: A conversion occurs when the user triggers an event, such as pressing a button or editing a value, and the program updates the result instantly.

### Task 6: EventRegistration + JDBC
- Why is JDBC required in the event registration application?
- What is the role of the database in storing registrations?
- Why is validation needed before insertion?
- Why use `PreparedStatement` for inserts and searches?
- Why is a unique constraint useful for event registrations?

Expected answer: The database stores persistent event registrations, while validation and prepared statements ensure correctness, security, and duplicate prevention.

## Short final summary
A strong viva answer should show that you understand: the Swing EDT, event-driven programming, layout design, background task handling, and safe database access. The main idea is that GUI code must remain responsive, data must be handled safely, and the design should separate responsibilities clearly.

## 5 one-line answers to remember
- The EDT should handle UI only; long tasks go in the background.
- Use `.equals()` instead of `==` for strings.
- `SwingWorker` keeps the interface responsive.
- `PreparedStatement` prevents SQL injection.
- Persistent drawings and database entries must be stored in model data, not just temporary graphics.
