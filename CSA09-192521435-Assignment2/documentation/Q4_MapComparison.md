# Q4 – Choosing a Map for a Pharmacy Inventory

## Aim

Compare `HashMap`, `TreeMap`, and `LinkedHashMap` for a pharmacy inventory that needs
fast stock lookups, a code-sorted report, and an insertion-order report. Also compare a
sorted array with binary search.

## Problem description

Each medicine is identified by a code and stores its name, quantity in stock, and unit
price. The sample inventory is inserted in reverse code order so that sorted-code and
insertion-order reports have visibly different orders.

## Objectives

- Test three inventory sizes.
- Measure 100,000 successful lookups per approach and inventory size.
- Measure construction time and both report-generation times.
- Verify the output order, rather than relying on an assumption about map iteration.
- Compare the maps with a sorted-array binary-search implementation.

## Concept explanation

- `HashMap` provides average constant-time `put` and `get`, but does not promise iteration
  order.
- `TreeMap` keeps keys sorted and supports `put` and `get` in logarithmic time.
- `LinkedHashMap` keeps entries in insertion order while providing average constant-time
  `put` and `get`.
- A sorted array allows binary search and a naturally sorted report, but inserting into
  its middle requires shifting elements.

## Initial prediction

Before measuring, the prediction was that `HashMap` and `LinkedHashMap` would be faster
than `TreeMap` for lookups, while `TreeMap` would produce the code-order report without
sorting. `LinkedHashMap` would provide the insertion-order report without a side list.
`HashMap` would require extra work for both report orders. A sorted array would make
code-order reporting simple but require `O(log n)` lookups and `O(n)` insertion shifts.

## Implementation approach

[`PharmacyInventoryBenchmark.java`](../Q4_MapComparison/PharmacyInventoryBenchmark.java)
creates reverse-ordered medicine data at sizes 100, 10,000, and 50,000. It measures:

- Building the collection.
- Exactly 100,000 successful stock lookups.
- Rendering a code-ordered report.
- Rendering an insertion-ordered report.

Each measurement has two warm-up runs and five timed repetitions using `System.nanoTime()`.
The lookup keys and medicine data are prepared outside the timed lookup region. Report
timings include any sorting or extra retrieval needed to create the requested order.

For code order, `TreeMap` iterates naturally. `HashMap` and `LinkedHashMap` sort a copied
entry list by key. For insertion order, `LinkedHashMap` iterates naturally; the other map
types use a recorded insertion-key list and retrieve each medicine. The sorted-array
approach uses a custom binary search and retains the original insertion-key list for the
second report.

## Algorithm

For each inventory size:

1. Create medicine records in descending code order and 100,000 lookup keys.
2. Build each map or sorted array and verify its natural order where applicable.
3. Generate both reports and verify each report's codes against the expected order.
4. Warm up each operation twice.
5. Record five build, lookup, and report durations per approach in CSV.
6. Print the five-run average for each measurement.

## Source code reference

- Benchmark, map implementations, reports, and sorted-array search:
  [`PharmacyInventoryBenchmark.java`](../Q4_MapComparison/PharmacyInventoryBenchmark.java)
- Raw timing samples: [`q4_map_benchmark.csv`](../experimental_data/q4_map_benchmark.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q4_MapComparison\out | Out-Null
javac -Xlint:all --release 17 -d Q4_MapComparison\out Q4_MapComparison\PharmacyInventoryBenchmark.java
java -cp Q4_MapComparison\out PharmacyInventoryBenchmark
```

The Java 17-targeted build and execution succeeded on JDK 21.0.11. The CSV path is
relative to the project root.

## Expected output

The program should confirm sorted-code and insertion-order reports for all four
approaches at inventory sizes 100, 10,000, and 50,000, print measured averages for build,
lookup, and report operations, and write its CSV. Numeric timings vary by run:

```text
Size     Approach                  Operation                  Average ms
<measured operation rows>
Verified <approach> report orders for inventory size <size>.
Warm-up runs: 2
Measured runs per approach, size, and operation: 5
Timing data: experimental_data\q4_map_benchmark.csv
```

## Actual execution output evidence

The program verified the required report ordering for every approach at all three sizes.
Actual output included:

```text
Size     Approach                  Operation                  Average ms
Verified HashMap report orders for inventory size 100.
...
Verified TreeMap report orders for inventory size 50000.
Verified LinkedHashMap report orders for inventory size 50000.
Verified SortedArrayBinarySearch report orders for inventory size 50000.
Warm-up runs: 2
Measured runs per approach, size, and operation: 5
Timing data: experimental_data\q4_map_benchmark.csv
```

## Experimental observations and comparison table

These are averages of the five `elapsed_ns` samples for each case. Report measurements
include report formatting plus the order-specific work described above.

| Inventory size | Approach | Build (ms) | 100,000 lookups (ms) | Code-order report (ms) | Insertion-order report (ms) |
|---:|---|---:|---:|---:|---:|
| 100 | HashMap | 0.046 | 14.080 | 0.887 | 0.771 |
| 100 | TreeMap | 0.279 | 11.948 | 0.192 | 0.144 |
| 100 | LinkedHashMap | 0.101 | 7.864 | 0.428 | 0.198 |
| 100 | SortedArrayBinarySearch | 0.111 | 18.127 | 0.205 | 0.188 |
| 10,000 | HashMap | 0.972 | 4.292 | 11.900 | 3.388 |
| 10,000 | TreeMap | 3.151 | 22.312 | 2.250 | 5.118 |
| 10,000 | LinkedHashMap | 0.614 | 3.292 | 2.704 | 2.098 |
| 10,000 | SortedArrayBinarySearch | 1.397 | 18.165 | 1.345 | 3.019 |
| 50,000 | HashMap | 3.241 | 4.320 | 24.206 | 11.749 |
| 50,000 | TreeMap | 16.968 | 27.665 | 10.597 | 20.677 |
| 50,000 | LinkedHashMap | 4.940 | 3.841 | 12.608 | 8.348 |
| 50,000 | SortedArrayBinarySearch | 3.679 | 22.813 | 7.585 | 17.072 |

These are measurements from one run on this machine, not universal timings. In
particular, the 100,000-lookup batch measures average throughput; it is not a measurement
of individual-request tail latency.

## Time complexity

| Operation | HashMap | TreeMap | LinkedHashMap | Sorted array |
|---|---|---|---|---|
| Build / insert one item | Average `O(1)` | `O(log n)` | Average `O(1)` | `O(n)` to keep the array sorted |
| Lookup one item | Average `O(1)` | `O(log n)` | Average `O(1)` | `O(log n)` binary search |
| Iterate all entries | `O(n)` | `O(n)` | `O(n)` | `O(n)` |
| Produce code-order report | `O(n log n)` to sort | `O(n)` iteration | `O(n log n)` to sort | `O(n)` iteration |
| Produce insertion-order report | `O(n)` with a key side list | `O(n log n)` with key-list lookups | `O(n)` iteration | `O(n log n)` with repeated binary searches |

The report complexities above include the extra work used by this implementation to
produce the requested order. A `TreeMap` lookup is logarithmic, while `HashMap` and
`LinkedHashMap` operations are average-case estimates that can degrade with poor hashing.

## Space complexity

Each approach stores `O(n)` medicine records. Hash maps use a table plus entry objects;
`LinkedHashMap` adds order links to its entries; `TreeMap` uses ordered tree nodes; and a
sorted array uses `O(n)` references in a contiguous array. The implementation also keeps
an insertion-key list for reports where the chosen structure does not preserve that
order. Exact per-entry byte counts depend on the JVM.

## Sorted-array and binarySearch alternative

The implemented array version sorts a `Medicine[]` by code and keeps a parallel sorted
code array. `Arrays.binarySearch()` finds a code's index, which is used to retrieve the
medicine at the same position. Lookup is `O(log n)` and the sorted report is a simple
`O(n)` scan.
Insertion into a maintained sorted array is `O(n)` because later elements must shift;
re-sorting a batch is `O(n log n)`. The array uses `O(n)` space. In this experiment,
the sorted array's 100,000 lookup batches took 18.127 ms, 18.165 ms, and 22.813 ms at
the three sizes, while its code report was quick because the array was already sorted.
Maintaining insertion order also required a separate list and repeated searches.

## Hashtable

`Hashtable` is a legacy synchronized map. It does not preserve insertion or sorted order
and disallows null keys and values. New code generally uses `HashMap` for ordinary
single-threaded access, `LinkedHashMap` or `TreeMap` when their ordering behavior is
needed, and `ConcurrentHashMap` for concurrent access. Synchronization alone does not
make `Hashtable` the best modern choice.

## Acceptability, analysis, and conclusion

For this exercise, an in-memory lookup target of **at most 1 ms per lookup** is defined
as acceptable for an interactive pharmacy screen. The largest observed batch was
27.665 ms for 100,000 `TreeMap` lookups, which is an average of about 0.277 microseconds
per lookup in this run. Every measured approach was below the chosen average-throughput
threshold; a production system should still measure individual latency, concurrent load,
and database/network costs.

At 50,000 records, `HashMap` and `LinkedHashMap` had similar 100,000-lookup times
(4.320 ms and 3.841 ms respectively). `LinkedHashMap` naturally supports the
insertion-order report. Its code-order report needs sorting, but in this run its two
report times totaled 20.956 ms, compared with 31.274 ms for `TreeMap` and 35.955 ms for
`HashMap`. On this evidence, `LinkedHashMap` is a reasonable choice when lookup and
insertion-order reporting are frequent and the code-order report can be sorted when
generated. Choose `TreeMap` instead if continuously sorted traversal is the primary
requirement. These results are small single-machine samples and should be rerun under
deployment-like conditions.

## Screenshot evidence

Capture the actual run manually as `output_screenshots/Q4/Q4_order_validation.png` and
`output_screenshots/Q4/Q4_benchmark_summary.png`. See the
[Q4 capture guide](../output_screenshots/Q4/README.md). No screenshots are included yet.
