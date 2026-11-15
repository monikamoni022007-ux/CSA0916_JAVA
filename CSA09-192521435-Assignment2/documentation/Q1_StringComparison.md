# Q1 – Building Text Under Load

## Aim

Compare `String`, `StringBuilder`, and `StringBuffer` for building railway ticket text,
and select an appropriate approach for individual tickets and a shared booking log.

## Problem description

A ticket is made by joining a sequence of fields. The experiment measures three different
batches: many short tickets, many fields in each ticket, and a smaller number of long
tickets. Results are specific to this machine and run; they are not universal Java timings.

## Objectives

- Predict how immutable and mutable text-building approaches scale.
- Compare repeated `+` concatenation with `StringBuilder` and `StringBuffer`.
- Record repeated elapsed times, estimated string-object counts, estimated character
  copying, and approximate retained heap.
- Demonstrate `StringJoiner` and `String.format()` as alternatives.
- Make an evidence-informed recommendation for ticket text and a shared log.

## Concept explanation

`String` is immutable: after a string is made, its contents cannot be changed. In a loop,
`ticket += field` produces a new combined string each time. Earlier prefixes have to be
copied again as the ticket grows.

`StringBuilder` keeps editable text in a resizable buffer. It is suitable when one thread
builds a ticket locally. `StringBuffer` has synchronized methods and is useful when the
same mutable buffer is accessed by multiple threads, though synchronization can cost time.
Neither class makes a sequence of multiple append calls one atomic operation.

`StringJoiner` is convenient for joining values with a delimiter. `String.format()` is
convenient for a fixed, readable template such as a ticket with named fields, but is
usually unnecessary for a high-volume loop that appends a variable number of fields.

## Initial prediction

Before measuring, the prediction was that `String +` would become slower as the number of
fields per ticket increased, because each append recreates and copies the growing prefix.
`StringBuilder` and `StringBuffer` were expected to scale approximately with the total
characters appended. `StringBuffer` could have additional synchronization overhead.
For a small fixed ticket, the difference might be negligible. The expected memory delta
was uncertain because heap snapshots include garbage-collector and runtime effects.

## Implementation approach

[`TextBuildingBenchmark.java`](../Q1_StringComparison/TextBuildingBenchmark.java) uses
three comparable implementations and reuses the same prebuilt field strings for all
approaches. Every timed repetition builds the specified number of tickets and consumes
each result's hash code. The program performs two warm-up runs and records five measured
runs per approach and workload using `System.nanoTime()`.

The workloads are:

| Workload | Tickets | Fields per ticket | Total field appends |
|---|---:|---:|---:|
| Many short tickets | 20,000 | 6 | 120,000 |
| Many fields per ticket | 10,000 | 50 | 500,000 |
| Long ticket | 200 | 200 | 40,000 |

The code checks that all three measured approaches produce identical text. It also checks
a `StringJoiner` example against the same sample and executes a fixed-field
`String.format()` example.

## Algorithm

For each approach and workload:

1. Create the workload's field strings once.
2. Build and consume tickets during two warm-up batches.
3. Time five batches with `System.nanoTime()` and write every raw duration to CSV.
4. Build and retain the output tickets for one post-GC heap snapshot.
5. Record the observed heap values and the estimated object/copy counts.

## Source code reference

- Main program and all implementations: [`TextBuildingBenchmark.java`](../Q1_StringComparison/TextBuildingBenchmark.java)
- Raw timing samples: [`q1_string_timing.csv`](../experimental_data/q1_string_timing.csv)
- Approximate retained-heap samples: [`q1_string_memory.csv`](../experimental_data/q1_string_memory.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q1_StringComparison\out | Out-Null
javac --release 17 -d Q1_StringComparison\out Q1_StringComparison\TextBuildingBenchmark.java
java -cp Q1_StringComparison\out TextBuildingBenchmark
```

The code was compiled with JDK 21.0.11 using `--release 17` and ran successfully. The
CSV paths are relative to the project root.

## Expected output

The program should print the two alternative-format examples, a timing line for each of
the three approaches and workloads, the warm-up and repetition counts, output-equivalence
confirmation, and the CSV paths. Timing values are machine- and run-dependent; the
following is the expected format, not a promise of specific timings:

```text
StringJoiner alternative: <ticket text>
String.format fixed-ticket alternative:
<formatted ticket fields>
many_short_tickets       String +       average: <measured> ms
...
Warm-up runs: 2
Measured runs per approach and workload: 5
All three measured approaches produced identical text.
Alternative StringJoiner and String.format examples ran successfully.
Timing data: experimental_data\q1_string_timing.csv
Retained-heap samples: experimental_data\q1_string_memory.csv
```

## Actual execution output evidence

Actual output from the completed run (rounded averages are computed from five raw samples):

```text
StringJoiner alternative: PNR=PNR123456;Passenger=Asha Kumar;
String.format fixed-ticket alternative:
PNR: PNR123456
Passenger: Asha Kumar
Coach: S2
Berth: 36
many_short_tickets       String +       average:      4.263 ms
  Approx. retained heap delta: 1,758,448 bytes (one post-GC sample; interpret cautiously)
many_short_tickets       StringBuilder  average:      4.682 ms
  Approx. retained heap delta: 1,989,264 bytes (one post-GC sample; interpret cautiously)
many_short_tickets       StringBuffer   average:      4.377 ms
  Approx. retained heap delta: 2,807,024 bytes (one post-GC sample; interpret cautiously)
many_fields_per_ticket   String +       average:     34.169 ms
  Approx. retained heap delta: 9,245,504 bytes (one post-GC sample; interpret cautiously)
many_fields_per_ticket   StringBuilder  average:      8.884 ms
  Approx. retained heap delta: 9,255,672 bytes (one post-GC sample; interpret cautiously)
many_fields_per_ticket   StringBuffer   average:      9.206 ms
  Approx. retained heap delta: 9,273,128 bytes (one post-GC sample; interpret cautiously)
long_ticket              String +       average:     13.116 ms
  Approx. retained heap delta: 765,640 bytes (one post-GC sample; interpret cautiously)
long_ticket              StringBuilder  average:      0.767 ms
  Approx. retained heap delta: 765,640 bytes (one post-GC sample; interpret cautiously)
long_ticket              StringBuffer   average:      1.156 ms
  Approx. retained heap delta: 765,640 bytes (one post-GC sample; interpret cautiously)
Warm-up runs: 2
Measured runs per approach and workload: 5
All three measured approaches produced identical text.
Alternative StringJoiner and String.format examples ran successfully.
Timing data: experimental_data\q1_string_timing.csv
Retained-heap samples: experimental_data\q1_string_memory.csv
```

## Experimental observations and measurements

### Average elapsed time

These are averages of the five `elapsed_ns` rows for each approach in the timing CSV.

| Workload | `String +` | `StringBuilder` | `StringBuffer` |
|---|---:|---:|---:|
| 20,000 × 6 fields | 4.263 ms | 4.682 ms | 4.377 ms |
| 10,000 × 50 fields | 34.169 ms | 8.884 ms | 9.206 ms |
| 200 × 200 fields | 13.116 ms | 0.767 ms | 1.156 ms |

### Estimated string objects and character copies

The following are estimates from the program, not JVM allocation-profiler counts.
The object estimate counts result `String` objects: one per `+` append versus one final
string per ticket for the mutable approaches. It excludes prebuilt input strings, builder
objects, backing arrays, and buffer resizing.

| Workload | Approach | Estimated result strings | Estimated characters copied |
|---|---|---:|---:|
| 20,000 × 6 | `String +` | 120,000 | 6,720,000 |
| 20,000 × 6 | `StringBuilder` / `StringBuffer` | 20,000 | 3,840,000 |
| 10,000 × 50 | `String +` | 500,000 | 220,400,000 |
| 10,000 × 50 | `StringBuilder` / `StringBuffer` | 10,000 | 17,600,000 |
| 200 × 200 | `String +` | 40,000 | 73,598,000 |
| 200 × 200 | `StringBuilder` / `StringBuffer` | 200 | 1,512,000 |

The copy estimate sums the growing prefix for `String +`. For a builder or buffer it
estimates writing the appended characters once and copying the completed text once in
`toString()`; actual buffer growth can add copies.

### Approximate retained heap

This is one post-GC snapshot per case while retaining all ticket outputs, not a measurement
of transient allocations or peak memory. `System.gc()` is only a request, so these numbers
are noisy and should not be used to claim that one implementation retains less memory.

| Workload | `String +` | `StringBuilder` | `StringBuffer` |
|---|---:|---:|---:|
| 20,000 × 6 | 1,758,448 bytes | 1,989,264 bytes | 2,807,024 bytes |
| 10,000 × 50 | 9,245,504 bytes | 9,255,672 bytes | 9,273,128 bytes |
| 200 × 200 | 765,640 bytes | 765,640 bytes | 765,640 bytes |

The retained outputs have the same count and text size for a given workload. Small
differences between snapshots are consistent with the limitations of a single heap sample;
an allocation profiler and repeated controlled heap measurements would be needed for a
strong memory comparison.

## Comparison and analysis

For the small six-field workload, `String +` was fastest in this run (4.263 ms versus
4.682 ms for `StringBuilder`). This is a useful reminder that a simple small expression
can be clearer and can perform well; the result should not be generalized beyond this
workload or machine.

As fields per ticket increased, the measured `String +` time rose relative to the mutable
approaches. For 50 fields it took 34.169 ms compared with 8.884 ms for `StringBuilder`.
For 200 fields it took 13.116 ms compared with 0.767 ms for `StringBuilder`. The estimated
copy counts show the same scaling reason: the growing prefix is copied repeatedly.

`StringBuffer` was similar to `StringBuilder` in these single-threaded tests. This run does
not measure contention between threads, so it cannot establish a multithreaded throughput
comparison. `StringJoiner` and `String.format()` were verified as alternatives but were
not included in the timed comparison.

## Time complexity

Let `f` be the number of fields and `L` the total characters in the completed ticket.

| Approach | Approximate building time | Reason |
|---|---|---|
| Repeated `String +` | `O(f × L)`; `O(f²)` for similarly sized fields | Each append recopies the accumulated prefix. |
| `StringBuilder` | Amortized `O(L)` | Appends write into a resizable buffer; occasional growth copies are amortized. |
| `StringBuffer` | Amortized `O(L)` plus synchronization | Similar buffer work with synchronized methods. |
| `StringJoiner` | `O(L)` | Joins values through mutable internal storage. |
| One fixed-template `String.format()` call | At least `O(L)`, with formatting overhead | Convenient for a known template; not benchmarked here. |

## Space complexity

Each approach needs `O(L)` space for the result. `String +` also creates up to one new
result string per field and generates substantial temporary allocation over the whole
build. A builder or buffer keeps a mutable backing array and creates the final `String`
when `toString()` is called; temporary buffer growth can require extra arrays. The exact
object layout and peak heap depend on the JVM.

## Design decision and conclusion

For a ticket assembled from a variable or large number of fields, use a local
`StringBuilder`: it avoids repeatedly copying the growing ticket and the experiment shows
the advantage on the larger-field workloads. For a small, fixed number of fields, `+` is
readable and was fastest in the small-ticket batch measured here.

If threads truly append to one shared mutable log buffer, `StringBuffer` synchronizes
individual methods. Append each already-built log entry in one call to avoid interleaving
its pieces. For a real high-volume service, a logging framework or a queue feeding a
dedicated writer is often a better design than a contended shared buffer.

The prediction that repeated `+` becomes less suitable as fields per ticket grow was
supported by both measured timings and estimated copy counts. The small-workload timing
did not support a blanket claim that `StringBuilder` is always faster. Heap snapshots
were too noisy for a reliable memory ranking.

## Screenshot evidence

Capture the latest actual run manually as `output_screenshots/Q1/Q1_run_latest.png`. An
earlier image, `output_screenshots/Q1/Q1_run.png.jpeg`, is present but its timings predate
the current CSV and should not be presented as evidence for the latest results. See the
[Q1 capture guide](../output_screenshots/Q1/README.md).
