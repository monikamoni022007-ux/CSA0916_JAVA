# Q2 – When Is ArrayList an Appropriate Engineering Choice?

## Aim

Compare `ArrayList`, `LinkedList`, and `ArrayDeque` for playlist-style operations and
decide which structure fits a playlist versus an Up Next queue.

## Problem description

A playlist supports appending songs, inserting or removing songs at the front and middle,
accessing the song at a chosen position, and iterating through all songs. The changed
condition adds an Up Next queue that frequently uses both ends.

## Objectives

- Measure seven operations at 1,000, 10,000, and 50,000 elements.
- Record five timed repetitions per operation, size, and collection.
- Explain the queue case and the operation limitations of `ArrayDeque`.
- Compare time and per-element space complexity, including a plain-array alternative.

## Concept explanation

`ArrayList` stores references in a resizable array. It offers constant-time indexed access
and fast iteration, but inserting or removing near the front or middle shifts elements.

`LinkedList` stores elements in nodes connected to neighboring nodes. Adding or removing
at either end is constant time. Finding an element by index still requires walking through
nodes, so using an index for a middle edit or access is linear time.

`ArrayDeque` is a resizable array-based double-ended queue. Its native operations are
at the ends; it does not provide indexed access or insertion. The benchmark uses an
explicit adapter for middle edits: it moves the suffix to a temporary deque, performs the
edit, and restores the suffix. That adapter is an illustration of the extra work required,
not a native `ArrayDeque` operation.

## Initial prediction

Before measuring, the prediction was that `ArrayList` would lead for indexed access and
be a reasonable playlist choice when appends, reads, and iteration dominate. Its front
edits should grow more expensive as the list grows. `LinkedList` and `ArrayDeque` should
be more suitable for frequent end operations. `LinkedList` indexed access and middle
operations were expected to include traversal cost; the `ArrayDeque` middle-edit adapter
was expected to be slow and use temporary space.

## Implementation approach

[`PlaylistBenchmark.java`](../Q2_ListComparison/PlaylistBenchmark.java) uses a small
type-safe `Sequence` interface so the same operation can be timed against all three
structures. Each measured batch starts with a pre-filled collection; setup is outside the
timed interval. The benchmark performs two warm-up runs followed by five measured runs.
It consumes a fingerprint of the final contents after timing so that the changes remain
observable, without timing the fingerprint itself.

| Initial size | Add/front operations per batch | Middle edits per batch | Indexed reads per batch | Full-iteration passes |
|---:|---:|---:|---:|---:|
| 1,000 | 100 | 50 | 50 | 5 |
| 10,000 | 100 | 50 | 50 | 5 |
| 50,000 | 100 | 50 | 50 | 5 |

`ArrayDeque` indexed reads walk from the nearer end. Its middle insertion/removal adapter
moves the suffix using a temporary deque. These results therefore include the adapter's
work and temporary allocation.

## Algorithm

For each starting size, operation, and structure:

1. Construct and fill the structure before timing.
2. Execute two warm-up batches.
3. Execute five batches timed with `System.nanoTime()`, writing each duration to CSV.
4. Consume the final contents outside the timed interval.
5. Print the mean time for the five samples.

The program separately checks front, end, and middle edits for each structure and prints
an Up Next queue example. The example uses `addLast` for ordinary additions, `addFirst`
for a song to play next, and `removeFirst` to play the next song.

## Source code reference

- Benchmark and queue demonstration: [`PlaylistBenchmark.java`](../Q2_ListComparison/PlaylistBenchmark.java)
- Raw timing samples: [`q2_list_benchmark.csv`](../experimental_data/q2_list_benchmark.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q2_ListComparison\out | Out-Null
javac --release 17 -d Q2_ListComparison\out Q2_ListComparison\PlaylistBenchmark.java
java -cp Q2_ListComparison\out PlaylistBenchmark
```

Compilation with `-Xlint:all --release 17` and execution both succeeded on JDK 21.0.11.
The benchmark CSV path is relative to the project root.

## Expected output

The program should demonstrate the Up Next ordering, report that all three collection
checks passed, print a timing summary for the measured operations, and write its CSV.
The benchmark values are machine- and run-dependent:

```text
Up Next first: Play Next
Up Next remaining: Track A -> Track B
ArrayList, LinkedList, and ArrayDeque operation checks passed.
Size     Structure        Operation          Average ms
<measured operation rows>
Warm-up runs: 2
Measured runs per operation, size, and structure: 5
Timing data: experimental_data\q2_list_benchmark.csv
```

## Actual execution output evidence

The run reported:

```text
Up Next first: Play Next
Up Next remaining: Track A -> Track B
ArrayList, LinkedList, and ArrayDeque operation checks passed.
Size     Structure        Operation          Average ms
...
Warm-up runs: 2
Measured runs per operation, size, and structure: 5
Timing data: experimental_data\q2_list_benchmark.csv
```

The full table below is calculated from the 315 raw timing rows written by that run. Each
number is the mean of five `elapsed_ns` samples, converted to milliseconds.

## Experimental observations and comparison table

| Size | Operation | ArrayList (ms) | LinkedList (ms) | ArrayDeque (ms) |
|---:|---|---:|---:|---:|
| 1,000 | Add at end | 0.025 | 0.019 | 0.047 |
| 1,000 | Insert at front | 0.163 | 0.040 | 0.058 |
| 1,000 | Remove at front | 0.079 | 0.033 | 0.049 |
| 1,000 | Insert in middle | 0.041 | 0.115 | 1.206 |
| 1,000 | Remove from middle | 0.015 | 0.199 | 0.753 |
| 1,000 | Access k-th element | 0.022 | 0.088 | 0.251 |
| 1,000 | Iterate (5 passes) | 0.455 | 0.412 | 0.718 |
| 10,000 | Add at end | 0.021 | 0.003 | 0.021 |
| 10,000 | Insert at front | 0.156 | 0.006 | 0.053 |
| 10,000 | Remove at front | 0.079 | 0.003 | 0.002 |
| 10,000 | Insert in middle | 0.035 | 0.545 | 4.196 |
| 10,000 | Remove from middle | 0.022 | 0.519 | 3.637 |
| 10,000 | Access k-th element | 0.003 | 0.319 | 0.996 |
| 10,000 | Iterate (5 passes) | 0.045 | 0.154 | 0.103 |
| 50,000 | Add at end | 0.118 | 0.003 | 0.125 |
| 50,000 | Insert at front | 0.613 | 0.004 | 0.135 |
| 50,000 | Remove at front | 0.347 | 0.004 | 0.002 |
| 50,000 | Insert in middle | 0.217 | 2.697 | 16.411 |
| 50,000 | Remove from middle | 0.097 | 3.103 | 15.683 |
| 50,000 | Access k-th element | 0.003 | 1.522 | 1.525 |
| 50,000 | Iterate (5 passes) | 0.269 | 0.698 | 0.355 |

Timings below one millisecond are especially sensitive to JIT compilation, garbage
collection, and machine load. These are averages from one process run with five samples,
not a formal JMH study. Use the raw CSV to inspect individual repetitions; do not treat
tiny timing differences as universal rankings.

## Time complexity

| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---|---|---|
| Add at end | Amortized `O(1)` | `O(1)` | Amortized `O(1)` |
| Insert at front | `O(n)` shifts | `O(1)` | Amortized `O(1)` |
| Remove at front | `O(n)` shifts | `O(1)` | Amortized `O(1)` |
| Insert in middle | `O(n)` shifts | `O(n)` to find index, then `O(1)` link | No native index operation; adapter `O(n)` |
| Remove from middle | `O(n)` shifts | `O(n)` to find index, then `O(1)` unlink | No native index operation; adapter `O(n)` |
| Access k-th element | `O(1)` | `O(n)` | `O(n)` by traversal |
| Iterate | `O(n)` | `O(n)` | `O(n)` |

For the benchmark's `ArrayDeque` middle-edit adapter, temporary extra space is `O(n)` in
the number of moved elements. A linked-list middle edit is `O(1)` only after a node or
list iterator is already at the target; locating an index is still `O(n)`.

## Space complexity

| Structure | Approximate storage per element |
|---|---|
| ArrayList | One array reference per slot, plus unused capacity |
| LinkedList | One node containing the element reference and link references (previous/next) |
| ArrayDeque | One array reference per occupied slot, plus unused capacity |

A plain fixed array also gives `O(1)` indexed access and `O(n)` iteration. Inserting or
removing at the front or middle requires manually shifting elements (`O(n)`). Appending
is `O(1)` while capacity remains; a resizable-array implementation occasionally copies
the array to a larger one, giving amortized `O(1)` append. Its element storage is `O(n)`.

## Analysis

The 50,000-element results show the expected cost of shifting for repeated front edits:
100 front insertions took 0.613 ms with `ArrayList`, while they took 0.004 ms with
`LinkedList` and 0.135 ms with `ArrayDeque` in this run. The same 100 front removals took
0.347 ms, 0.004 ms, and 0.002 ms respectively.

For position-based edits, the benchmark instead shows `ArrayList` ahead at the middle:
50 inserts took 0.217 ms, versus 2.697 ms for indexed `LinkedList` edits and 16.411 ms
for the temporary-deque adapter. The linked list must find the requested index, and the
adapter must move and restore elements. This demonstrates why a linked list is not
automatically the best choice for arbitrary indexed edits.

For 50 indexed reads at size 50,000, `ArrayList` took 0.003 ms, compared with 1.522 ms
for `LinkedList` and 1.525 ms for the deque traversal adapter. This is consistent with
constant-time array indexing versus traversal. Small timings fluctuate and should be
retested if a deployment decision depends on a narrow difference.

## Conclusion and engineering choice

`ArrayList` is a reasonable playlist choice when users mostly append, jump to a position,
and browse or iterate. I would stop recommending it when frequent edits at the front of a
large list become a measured bottleneck. If the changed requirement is an Up Next queue
with additions at both ends and removal from the front, use `ArrayDeque`; its native
end operations fit the workload without index-adapter overhead.

For arbitrary middle edits, consider how the target is found: a `LinkedList` is useful if
the caller already has an iterator at the edit point, but index-based edits still require
traversal. The actual measurements support these workload-specific choices rather than a
single collection being best for every playlist operation.

## Screenshot evidence

Capture the latest run manually as
`output_screenshots/Q2/Q2_run_latest_part1.png`,
`output_screenshots/Q2/Q2_run_latest_part2.png`, and
`output_screenshots/Q2/Q2_run_latest_part3.png`. Three earlier images are present, but
their timings predate the current CSV and should not be submitted as evidence for the
latest results. See the [Q2 capture guide](../output_screenshots/Q2/README.md).
