# Q7 â€“ Diagnose, Redesign and Validate

## A. Diagnose the bottleneck

Q1 identified repeated `String +` concatenation as a limitation when a ticket contains
many fields. For the selected 10,000-ticket, 50-field workload, the estimated copied
characters are 220,400,000 for `String +` and 17,600,000 for a pre-sized `StringBuilder`.

The limitation is the repeated copying of the growing immutable string prefix. The work
grows with both the field count and the accumulated ticket length.

## B. Define the design objective

For a controlled 10,000-ticket workload with 50 identical prebuilt fields per ticket:

- Produce exactly the same ticket text with both implementations.
- Compare both implementations on the same input and JVM process.
- Achieve at least **30% lower average elapsed time** for the redesign over ten measured
  repetitions.
- Record raw time samples, estimated string-result objects and character copies, and an
  explicitly approximate retained-heap sample.

## C. Design the revised solution

### Original

For each ticket, start with an empty `String` and append every field using `+=`. Each
append creates a new result string and copies the growing prefix.

### Redesigned

For each ticket, create a local `StringBuilder` with a capacity equal to the known total
field length, append each field, and call `toString()` once. Pre-sizing avoids unnecessary
builder-buffer growth when the ticket size is known.

### Pseudocode

```text
fields = prepare the same 50 field strings once
capacity = sum of all field lengths
repeat 10 times for each approach:
    for each of 10,000 tickets:
        original: concatenate fields into immutable String with +
        redesign: append fields into pre-sized StringBuilder, then toString
        consume the result hash
compare checksums, timings, estimated copies and retained-heap samples
```

The redesign avoids creating a new complete prefix string on every append. It still needs
a final `String` per ticket and a mutable builder buffer while constructing each ticket.

## D. Predict before implementing

Based on Q1's repeated five-run comparison, I predicted that the pre-sized builder would
reduce average elapsed time by at least 30% for this workload. I also predicted fewer
intermediate result `String` objects and fewer characters copied. This is a prediction,
not a recorded result; the dedicated run below tests it.

## Implementation and algorithm

[`DesignChallenge.java`](../Q7_DesignChallenge/DesignChallenge.java) prepares one shared
set of fields and uses it for both approaches. It performs two warm-up runs per approach,
then ten measured runs, alternating which approach runs first. Each timed batch builds
10,000 tickets and consumes each ticket's hash. Checksums are compared to ensure both
approaches generated the same output.

Estimated object/copy counts are derived from the algorithm, not an allocation profiler.
The retained-heap sample retains the final tickets and uses `System.gc()` before and
after a single sample; it does not measure transient allocation or peak memory.

## Source code references

- Original and redesigned implementations: [`DesignChallenge.java`](../Q7_DesignChallenge/DesignChallenge.java)
- Raw timing samples: [`q7_design_timing.csv`](../experimental_data/q7_design_timing.csv)
- Approximate retained-heap samples: [`q7_design_memory.csv`](../experimental_data/q7_design_memory.csv)
- Q1 evidence used for the diagnosis: [`q1_string_timing.csv`](../experimental_data/q1_string_timing.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q7_DesignChallenge\out | Out-Null
javac -Xlint:all --release 17 -d Q7_DesignChallenge\out Q7_DesignChallenge\DesignChallenge.java
java -cp Q7_DesignChallenge\out DesignChallenge
```

Compilation targeting Java 17 and execution succeeded on JDK 21.0.11 with no compiler
warnings.

## Expected output

The program should verify equal output, report ten measured runs per approach, print the
mean elapsed times and computed reduction, state whether the 30% objective was met, and
confirm matching checksums. The exact timing and retained-heap values vary by run. A
single approximate retained-heap sample must not be presented as evidence of memory
improvement.

## Actual execution output evidence

The output and timing summary from the completed run are:

```text
Workload: 10000 tickets x 50 fields
Warm-up runs per approach: 2
Measured runs per approach: 10
Output equivalence check passed; characters per ticket: 880
String + average: 39.504 ms
Pre-sized StringBuilder average: 7.173 ms
Elapsed-time reduction: 81.8%
Objective (at least 30% reduction): met
Estimated strings per batch: 500,000 vs. 10,000
Both approaches produced matching checksums in every run.
String + approximate retained heap delta: 8,200,608 bytes
Pre-sized StringBuilder approximate retained heap delta: 9,369,704 bytes
Heap values are single post-GC samples; interpret cautiously.
```

## Experimental observations and measurements

The controlled dataset contains 10,000 tickets, 50 fields per ticket, and 500,000 total
field appends per measured batch. Each method has ten raw timing samples. The CSV records
the individual values and algorithmic estimates.

| Implementation | Average elapsed time | Change vs. original | Estimated `String` objects per batch | Estimated characters copied |
|---|---:|---:|---:|---:|
| Original `String +` | 39.504 ms | Baseline | 500,000 | 220,400,000 |
| Pre-sized `StringBuilder` redesign | 7.173 ms | 81.8% lower | 10,000 | 17,600,000 |

The estimated string-object and character-copy counts follow from the algorithm; they
are not allocation-profiler measurements. Retained-heap deltas were 8,200,608 bytes
for `String +` and 9,369,704 bytes for the builder in this single sample. The builder
sample was higher, so these approximate retained-heap measurements do not show a memory
reduction. They measure retained outputs after GC, not transient allocations or peak
memory, and are especially sensitive to GC and JVM heap behavior.

## Time complexity

For `f` fields and `L` total output characters per ticket:

- Repeated concatenation recopies each growing prefix, giving approximately `O(f Ã— L)`;
  with similarly sized fields this is `O(fÂ²)` in the number of fields.
- The pre-sized builder appends `L` characters and copies the result once, giving
  `O(L)` time per ticket.
- Across `t` tickets, these costs are approximately `O(t Ã— f Ã— L)` and `O(t Ã— L)`.

## Space complexity

Both approaches need `O(L)` space for one final ticket. The original creates up to `f`
intermediate result strings while building one ticket, causing additional temporary
allocation. The redesign uses a builder and its backing array plus the final string;
pre-sizing avoids extra growth arrays but means the builder temporarily holds capacity for
the full ticket.

## Analysis, trade-offs, and final decision

The measured 81.8% elapsed-time reduction exceeded the 30% objective, and checksums
matched in every run, so the pre-run prediction was met for this workload. The algorithmic
estimates also show fewer intermediate `String` objects and fewer copied characters.
However, the single approximate retained-heap sample was higher for the builder and does
not demonstrate lower retained memory. The builder has slightly more setup than a short
`+` expression, so the original can still be clearer for a few fixed fields. For this
50-field workload, the pre-sized builder is preferred for execution time, not on the basis
of the heap sample.

## Screenshot evidence

Capture the actual run manually as
`output_screenshots/Q7/Q7_equivalence_and_timing.png` and
`output_screenshots/Q7/Q7_heap_sample_caveat.png`. See the
[Q7 capture guide](../output_screenshots/Q7/README.md). No screenshots are included yet.

