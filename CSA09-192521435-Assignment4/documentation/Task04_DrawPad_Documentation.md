# Task 04 – DrawPad Event-Handling Forensics

## 1. Objective
The DrawPad program demonstrates event-driven drawing using Java AWT. It must support mouse-based drawing, keyboard-driven clear/draw actions, and a visible click count display.

## 2. Implementation evidence
Primary source file:
- `Task04_DrawPad/src/DrawPad.java`

Verified runtime evidence:
- `output_screenshots/Task04/01_startup.png`
- `output_screenshots/Task04/02_mouse_drawing.png`
- `output_screenshots/Task04/03_keyboard_clear.png`
- `output_screenshots/Task04/04_keyboard_drawing.png`
- `output_screenshots/Task04/05_final_canvas_state.png`

## 3. Required GUI behaviour
- The frame opens as a white drawing canvas titled `DrawPad`.
- Clicking on the drawing area adds a `Point` to the list.
- The label shows the count of points currently on the canvas.
- The `Clear` button removes all stored points and resets the count.
- Keyboard actions may also clear or add points without using the mouse.
- `paint(Graphics)` redraws all stored points whenever the frame repaints.

## 4. Mouse event handling
The source uses `addMouseListener(new MouseAdapter() { ... })` and listens for `mouseClicked(MouseEvent e)`.

| Event | Trigger | Handler action | Result |
|---|---|---|---|
| `mouseClicked` | User clicks inside the frame | `points.add(e.getPoint())` and update label | A black dot appears at the clicked position |
| Repaint | After each click | `repaint()` | The canvas refreshes and preserves all points |

### Mouse event pseudocode
```java
on mouse clicked:
    point = current cursor location
    points.add(point)
    clickCountLabel.setText("Click Count: " + points.size())
    repaint()
```

## 5. Keyboard event handling
The source uses `addKeyListener(new KeyAdapter() { ... keyPressed(KeyEvent e) ... })`.

| Key / action | Handler response | Purpose |
|---|---|---|
| `Delete`, `Backspace`, `Escape`, `c` | `clearDrawing()` | Clear the drawing canvas |
| `Space`, `Up`, `Down`, `Left`, `Right`, `d`, `p` | Add a point at a safe location and repaint | Test keyboard-driven drawing support |

### Keyboard event pseudocode
```java
on keyPressed:
    keyCode = e.getKeyCode()
    keyChar = lowercase(e.getKeyChar())

    if keyCode is DELETE/BACK_SPACE/ESCAPE or keyChar == 'c':
        clearDrawing()
        return

    if keyCode is SPACE/UP/DOWN/LEFT/RIGHT or keyChar == 'd' or keyChar == 'p':
        add a point near the middle of the canvas
        update click count
        repaint()
```

## 6. Event sequence explanation
1. The frame is created and initializes the layout, label, and button.
2. The mouse listener captures each click and adds a point.
3. The keyboard listener captures key input and either clears or adds points.
4. `repaint()` requests a redraw.
5. `paint(Graphics)` traverses the `points` list and draws a small filled oval for each point.
6. The UI remains responsive because each event performs only a small, local update.

## 7. Diagrammatic flow
```text
Frame created
   -> set layout / label / button
   -> attach MouseAdapter
   -> attach KeyAdapter
   -> user action
        -> mouse click or key press
        -> update ArrayList<Point>
        -> update label
        -> repaint()
        -> paint(Graphics)
        -> draw point(s)
```

## 8. Complexity analysis
- Adding one point: O(1)
- Clearing all points: O(n) because the list is emptied
- Redrawing all points: O(n)
- Memory usage: O(n), where n is the number of stored points

This matches the event-driven design of the canvas because it stores only the visible drawing points instead of re-creating pixels from external state.

## 9. Validation summary
The Task 04 implementation was compiled and executed successfully using the actual Java application. The runtime evidence confirms:
- startup view displayed correctly
- mouse drawing works
- keyboard clear works
- keyboard drawing works
- final canvas state remains intact after the action sequence

## 10. Remaining note
The file `Task04_DrawPad/src/DrawPad.java` is the authoritative source for this task. The screenshot evidence under `output_screenshots/Task04/` is the runtime proof captured from the live Java application.
