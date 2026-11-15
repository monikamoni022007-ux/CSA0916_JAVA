# Q6 – Priority, Order and Fairness in a Queue

## Aim

Build an emergency triage queue that serves the most severe patient first, preserves
arrival order for equal severity, completes the supplied TODO, and measures queue growth.

## Problem description

Patients have severity values from 1 (critical) to 5 (minor). Severity must take
precedence over arrival time, but patients with the same severity must remain FIFO.

## Objectives

- Implement severity priority with a `PriorityQueue`.
- Preserve FIFO order for patients of equal severity.
- Verify the supplied program's expected output.
- Test an independently designed eight-patient order.
- Measure admitting and serving at three queue sizes.
- Explain priority-queue iteration, starvation, alternatives, complexity, and deployment.

## Concept explanation

A plain FIFO `Queue` always serves the earliest arrival. It cannot serve a later critical
patient before an earlier minor patient, so it does not implement the triage rule.

The priority queue compares patients first by severity in ascending numerical order:
severity 1 comes first. The second comparison uses an arrival counter in ascending order.
The counter increases with every admission, so equal-severity patients are served FIFO.

`PriorityQueue` iteration and its `toString()` do not promise priority order. To display a
waiting list in treatment order without changing the real queue, the implementation copies
the queue and repeatedly polls the copy.

## Initial prediction

For a binary-heap priority queue, admission and removal of the next patient should each
take `O(log n)` time, while looking at the next patient takes `O(1)`. A batch of `n`
admissions or removals should therefore take approximately `O(n log n)`. Timing will
vary with JIT compilation, garbage collection, and machine load.

## Implementation approach

- [`Patient.java`](../Q6_HospitalTriage/Patient.java) holds immutable name, severity, and
  arrival fields and rejects severity values outside 1–5.
- [`Triage.java`](../Q6_HospitalTriage/Triage.java) completes TODO 1 with a
  severity-then-arrival comparator and keeps the supplied six-patient `main` program.
- [`TriageExperiments.java`](../Q6_HospitalTriage/TriageExperiments.java) verifies a
  hand-checked eight-patient order and measures admissions and service operations.

The priority queue declaration directly uses `Comparator.comparingInt()` followed by
`thenComparingLong()`. The counter is unique for each admitted patient, so the comparator
also provides a deterministic order when severity is equal.

## Algorithm

### Admit

1. Make a patient using the current arrival counter.
2. Increment the counter.
3. Add the patient to the priority queue, which places it by severity and arrival order.

### Serve next

1. Call `poll()` to remove and return the highest-priority (lowest severity number)
   patient.
2. If the queue is empty, `poll()` returns `null`.

### Display waiting order

1. Copy the priority queue.
2. Repeatedly poll the copy into a list.
3. Leave the live queue unchanged.

## Source code references

- Patient model: [`Patient.java`](../Q6_HospitalTriage/Patient.java)
- Completed supplied program: [`Triage.java`](../Q6_HospitalTriage/Triage.java)
- Eight-patient test and performance experiment: [`TriageExperiments.java`](../Q6_HospitalTriage/TriageExperiments.java)
- Hand-checked treatment-order evidence: [`q6_triage_results.csv`](../experimental_data/q6_triage_results.csv)
- Raw timing samples: [`q6_triage_benchmark.csv`](../experimental_data/q6_triage_benchmark.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q6_HospitalTriage\out | Out-Null
javac -Xlint:all --release 17 -d Q6_HospitalTriage\out Q6_HospitalTriage\Patient.java Q6_HospitalTriage\Triage.java Q6_HospitalTriage\TriageExperiments.java
java -cp Q6_HospitalTriage\out Triage
java -cp Q6_HospitalTriage\out TriageExperiments
```

Compilation targeting Java 17 and both executions succeeded on JDK 21.0.11 with no
compiler warnings.

## Expected output

The supplied six-patient program should print the following exact treatment order:

```text
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
```

The experiment should also print the verified eight-patient order and a benchmark table.
Timing values vary by run; queue sizes, warm-up/repetition counts, successful checks, and
CSV paths should be visible.

## Actual execution output evidence

The completed supplied program produced the required exact output:

```text
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
```

The eight-patient experiment produced:

```text
Eight-patient treatment order (queue remains unchanged):
[Ben(S1), Fay(S1), Eli(S2), Hana(S2), Ava(S3), Cara(S3), Gus(S3), Dev(S5)]
Treatment-order checks passed for all 8 patients.
Eight-patient results: experimental_data\q6_triage_results.csv
```

The repeated benchmark output included:

```text
Queue size Operation            Average ms
100        admit_all                 0.288
100        serve_all                 0.171
10000      admit_all                 2.958
10000      serve_all                 4.008
50000      admit_all                 5.063
50000      serve_all                23.202
Warm-up runs: 2
Measured runs per queue size and operation: 5
Performance data: experimental_data\q6_triage_benchmark.csv
```

## Experimental observations: hand-checked correctness

The test admits Ava (3), Ben (1), Cara (3), Dev (5), Eli (2), Fay (1), Gus (3), and Hana
(2), in that order. Sorting by severity and then arrival predicts:

| Position | Patient | Severity | Reason |
|---:|---|---:|---|
| 1 | Ben | 1 | First severity-1 arrival |
| 2 | Fay | 1 | Second severity-1 arrival |
| 3 | Eli | 2 | First severity-2 arrival |
| 4 | Hana | 2 | Second severity-2 arrival |
| 5 | Ava | 3 | First severity-3 arrival |
| 6 | Cara | 3 | Second severity-3 arrival |
| 7 | Gus | 3 | Third severity-3 arrival |
| 8 | Dev | 5 | Lowest remaining priority |

All eight names and severity levels matched the observed treatment order.

## Experimental observations: performance measurements

The benchmark runs two warm-up batches and records five samples per queue size and
operation. Admission timing includes constructing each patient and adding it to an empty
queue. Serving setup is performed before timing; the timed region polls every patient and
consumes a small checksum. The CSV contains each raw duration.

| Queue size | Admit all (average ms) | Serve all (average ms) |
|---:|---:|---:|
| 100 | 0.288 | 0.171 |
| 10,000 | 2.958 | 4.008 |
| 50,000 | 5.063 | 23.202 |

These are averages from one run on this machine, not universal service-time guarantees.
The admission result did not increase monotonically between 10,000 and 50,000 in this
run; JIT, allocation, and garbage collection can make such small measurements noisy.
The larger service batch did take longer, consistent with removing more heap entries.
The CSV preserves all five individual measurements for review.

## Time complexity

| Operation | PriorityQueue |
|---|---|
| Admit one patient | `O(log n)` |
| View next patient with `peek()` | `O(1)` |
| Serve next patient with `poll()` | `O(log n)` |
| Serve all `n` patients | `O(n log n)` |
| Copy and display waiting order | `O(n)` copy plus `O(n log n)` polling |

## Space complexity

The priority queue stores `O(n)` patient references and heap data. Patient objects and
the arrival counter use `O(n)` and `O(1)` space respectively. Creating an ordered display
copy takes another `O(n)` references.

## Alternative implementations

### Sorted LinkedList

Keep the list sorted by severity and arrival. Finding the insertion position takes
`O(n)`; inserting at that already-found position is `O(1)`. Serving the first item is
`O(1)`. Total storage is `O(n)` with node overhead. This may be reasonable for small
queues, but each admission scans the list.

### Five FIFO queues

Use one FIFO queue for each severity level. Admission selects the matching queue and
appends in `O(1)`. To serve, check levels 1 through 5 and remove from the first non-empty
queue; with exactly five severity categories this is `O(1)`. Storage remains `O(n)`.
This is a simple option when severity values are a small fixed set.

## Starvation, fairness, and deployment decision

A severity-5 patient can wait indefinitely if severity-1 to severity-4 patients keep
arriving, because strict severity priority always chooses the more urgent patients first.
One proposed aging policy is to improve a waiting patient's effective priority by one
severity level after each configured waiting interval, eventually allowing the patient to
reach level 1. Continue to order equal effective severities by arrival. Because a patient's
effective priority changes with time, the queue must be periodically rebuilt or aged
patients reinserted; a normal heap does not reorder itself when a comparator's result
changes. Clinical staff should retain the ability to reassess severity and override
automatic aging.

Five thousand arrivals per day is about one arrival every 17 seconds on average, though
real hospital traffic is bursty. The experiment successfully queued 50,000 patients and
served the batch in about 23 ms on this machine, so the heap operations are not an obvious
computational limit for 5,000 admissions a day. That does not make this in-memory demo a
complete hospital deployment: a production system also needs persistence, concurrent
admissions, recovery, audit history, privacy controls, and clinically validated escalation
rules.

## Analysis and conclusion

The exact supplied output verified severity priority and arrival-order ties. The
independent eight-patient scenario verified FIFO order among severities 1, 2, and 3. A
priority queue is more suitable than a plain FIFO queue for strict triage, and its
`O(log n)` admission and service costs are appropriate for the tested sizes. Aging can
reduce starvation, but its dynamic priorities require explicit queue maintenance and
clinical safeguards.

## Screenshot evidence

Capture the actual runs manually as `output_screenshots/Q6/Q6_supplied_triage_order.png`,
`output_screenshots/Q6/Q6_eight_patient_validation.png`, and
`output_screenshots/Q6/Q6_benchmark_summary.png`. See the
[Q6 capture guide](../output_screenshots/Q6/README.md). No screenshots are included yet.
