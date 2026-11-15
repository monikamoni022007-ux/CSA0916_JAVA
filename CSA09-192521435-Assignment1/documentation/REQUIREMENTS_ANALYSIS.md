# Requirements Analysis

## Primary source

The official source is `ASSIGNMENT 1.pdf` (CSA09, Java Fundamentals and Object-Oriented Programming), 13 pages, 50 marks. The supplied `JAVA 1 ASS ANSWER.docx` was inspected as a draft reference; it contains assumptions and example rates that are not all stated in the official PDF. Where it conflicts with or exceeds the PDF, this analysis follows the PDF.

## Scenario and shared rules

The campus has a multi-floor car park used by students, staff and visitors. Vehicles include two-wheelers, cars and electric vehicles. An arriving vehicle receives a suitable free slot and unique ticket with entry time. At exit, the slot is released and a parking fee is calculated. Staff and students with a valid monthly pass park free; visitors pay the normal parking fee. EV charging is billed separately. Facility examples include 1 floor × 20 slots, 3 floors × 50 slots, and 5 floors × 100 slots.

### Inputs and outputs

- Entry: vehicle category and registration number, parking user, entry timestamp, and (for an EV exit) measured charging energy in kWh.
- Exit: active registration number, exit timestamp, and optional EV energy usage.
- Outputs: ticket number, floor/slot coordinates, fee, and success/failure for the operation.

### Core business rules

1. A slot contains at most one vehicle.
2. Ticket identifiers are monotonically increasing and not reused.
3. An active registration cannot be parked twice.
4. An invalid exit does not release the vehicle's slot.
5. A fee cannot be calculated for a negative stay or negative energy usage.
6. Parking uses the hourly rates shown by the official Task 8 `FeeDemo`: Rs 10/hour for a two-wheeler, Rs 30/hour for a car/EV, and EV charging at Rs 12/kWh. Each started hour is billed in full in that demo and the core non-van tariff.
7. A valid staff/student monthly pass waives parking charges; EV charging remains separately payable.
8. The official PDF does not give a daily maximum, so the implementation does not invent one.
9. Task 12 requires an e-bike flat fee but supplies no amount. The implementation therefore requires the amount to be injected/configured and does not choose a default.
10. The PDF says a visitor may buy a one-day pass but gives no price or explicit parking-fee waiver. By user decision, the pass is a one-day access credential only; visitors still pay their normal parking fee. No purchase price is represented.
11. Task 12 specifies double the car rate from 9:00 AM through 5:00 PM. For partial-hour overlaps, the implementation prorates by actual elapsed time in each rate window; the PDF does not separately state rounding for these time-window segments.
12. Floors use independent capacities; no equal-capacity assumption is made.

## Task-by-task requirements

| Task | Marks | Required evidence and implementation target |
| --- | ---: | --- |
| 1. Problem analysis and objects | 4 | Explain system inputs/outputs, noun-to-class and verb-to-method analysis, responsibilities, invariants and assumptions. Compare cohesive vehicle objects with parallel `regNo[]`, `type[]`, `entryTime[]` arrays for search time and per-vehicle storage. |
| 2. Data representation | 3 | Justify types for slot, registration, time, duration and fee; choose a floor/slot representation; explain input and casting/precision risk with a fee example. Compare jagged arrays, a flat one-dimensional array and `ArrayList<String[]>` for unequal floors. |
| 3. Control logic | 4 | Give standard pseudocode for allocation and fee calculation; explain search termination, full facility, fee slabs, `break`/`continue`/`return`. Complete `ParkingFacility.allocate` and `release` without changing the supplied `main()`, and match its expected output. Compare scanning against per-floor free counters/stacks. |
| 4. Classes, objects and constructors | 4 | Specify fields/methods, constructor overloads/chaining and `this`; decide whether copying a ticket is safe; explain stack references and heap objects. Compare constructors, setter initialization and factories for validity and cost. |
| 5. Static members and object lifecycle | 3 | Justify class-wide ticket numbering and other static members; explain object passing/return, garbage-collection eligibility, and static versus facility-instance counters across multiple gates. |
| 6. Inheritance hierarchy | 4 | Design vehicle and optional user hierarchies; identify inherited/specialized state and behavior; show `super` use and the `ElectricCar` constructor trace. Provide a UML diagram and compare inheritance with composition. |
| 7. Overriding | 4 | Demonstrate specialized methods, `@Override`, valid override rules and appropriate `final` use. Implement `toString`, `equals` and `hashCode`; compare overriding with a type-field switch. |
| 8. Polymorphism | 4 | Demonstrate overload resolution, upcasting and dynamic dispatch with a `Vehicle[]`; trace a call. Complete `FeeDemo` without changing its `main()` and match all four expected output lines. Explain registration-based equality for the final `true`. |
| 9. Abstraction | 4 | Use an abstract class and an interface, justify their roles and multiple capabilities, and explain default/static interface methods. Compare with reversing the abstract-class/interface roles. |
| 10. Packages and encapsulation | 3 | Organize classes into packages; justify access modifiers; validate through constructors/accessors; discuss wrappers/autoboxing and the risks of public fields. |
| 11. Implementation, testing and defence | 5 | Integrate the system; test normal, boundary and invalid operations, including last slot, zero minutes, fee boundaries, unknown type, duplicate registration and invalid slot. Hand-calculate daily collection. Evaluate OOP design and a limitation. Compare array, `ArrayList` and `HashMap` ticket lookup. |
| 12. Day-4 design challenge | 8 | Analyze impact and retain/modify/redesign. Implement e-bikes with a flat-fee parameter, delivery vans with peak-window pricing, a one-day visitor pass, and five unequal-capacity floors. Defend the decision with changed classes and actual tests. |

## Required code demonstrations and expected output

### Task 3 `ParkingFacility`

The supplied main must print:

```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

### Task 8 `FeeDemo`

The supplied main must print:

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

### Task 11 / 12

Task 11 integrates entry, tickets, parking, exit, user passes, EV energy and fees. Task 12 extends it with configurable e-bike pricing, delivery-van peak-window pricing, one-day visitor access and five unequal floors.

## Submission requirements

- One task directory for each of Tasks 1–12, with source, execution instructions and evidence.
- README with student name, register number, Java version, compile/run directions and per-task result.
- At least one progressive commit per task in a GitHub repository named `CSA09-<RegisterNumber>-Assignment1`; a working repository link is mandatory.
- Report with UML, pseudocode, complexity analysis, test tables and discussion.
- Student must be prepared to explain/execute/modify submitted code in a verification/viva.

Student name (Kavya Sree T) and college register number (192524444) are supplied. The GitHub repository/link remains pending until the repository is created; no repository was pushed.
