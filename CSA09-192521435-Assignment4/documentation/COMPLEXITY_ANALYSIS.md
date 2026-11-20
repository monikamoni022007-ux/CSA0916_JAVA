# Complexity Analysis

## Task 01 Fee Window
- Validation: O(1)
- Payment processing: O(1) for a single insert
- Memory: O(1)

## Task 02 Appointment Desk
- Department-to-doctor lookup: O(1) average with Map
- Refreshing doctors: O(k)

## Task 03 Library Search
- Search over n records: O(n)
- Pagination of page size k: O(k)

## Task 04 DrawPad
- Add point: O(1)
- Redraw all points: O(n)
- Memory: O(n)

## Task 05 Currency Converter
- Conversion: O(1)
- Listener registration: O(1)

## Task 06 Event Registration
- Validation: O(1)
- SQL filtering: O(m) on server side
- Pagination: O(pageSize)
- Loading all records into memory would be O(n) and less responsive
