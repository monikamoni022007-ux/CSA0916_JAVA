# Task 04 – DrawPad

## Defects identified
- `mouseclicked()` is wrong; the callback must be `mouseClicked()`.
- The click count label must update after every click.
- `getGraphics()` is not persistent and cannot preserve drawings after repaint events.
- The drawing disappears after minimize/restore because the graphics context is temporary.

## Correct approach
The repaired program stores points in an `ArrayList<Point>`, updates the count label, calls `repaint()`, and redraws stored points inside `paint(Graphics)`.
