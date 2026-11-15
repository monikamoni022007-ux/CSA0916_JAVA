# Q5 – Derive a Generic Ranking Component from a Principle

## Aim

Build a reusable generic leaderboard with a correct ranking order, complete both supplied
TODOs, and demonstrate the failure caused by a deliberately faulty comparator.

## Problem description

Sports teams are ranked by points (higher first), then goal difference (higher first),
then name (A–Z). The ranking component should be reusable with other element types, such
as players or quiz teams, without making one ranking rule part of every model class.

## Objectives

- Implement `Leaderboard<T>` with a comparator and a copied, sorted ranking result.
- Complete TODO 1 and TODO 2 from the assignment program.
- Use `Comparator.comparingInt()` and `thenComparing()` for the required ordering.
- Verify the exact supplied expected output and additional ties at every ranking level.
- Test a faulty comparator and explain why the correct comparator is consistent.

## Concept explanation

`Comparable<T>` defines one natural order on the class itself. `Comparator<T>` supplies an
external ordering rule. Here a `Comparator<Team>` is preferable: a team can be ranked
differently for other reports, and the same generic `Leaderboard<T>` can rank other types.
The `Team` class does not need to know about leaderboard sorting.

`Leaderboard<T>` accepts a `Comparator<? super T>`. The `? super T` wildcard means the
comparator may compare `T` values or values of a parent type. The `addAll(List<? extends
T>)` method accepts a list of `T` or a list of a subtype of `T`, while the leaderboard
stores its own `T` items.

`ranking()` copies the internal list and sorts the copy. Calling it does not change the
original insertion order.

## Initial prediction

The comparator chain should put larger point values first. When points tie, larger goal
difference should come first. When both numeric values tie, alphabetical team name should
decide. The order should be transitive because it compares a sequence of well-defined
orders lexicographically.

The deliberately faulty comparator subtracts integer scores. Extreme `int` values can
overflow, making the result contradict the desired ordering. Ignoring tie-breakers can
also return zero for teams that should be ordered differently.

## Implementation approach

[`LeaderboardDemo.java`](../Q5_GenericLeaderboard/LeaderboardDemo.java) contains:

- An immutable-field `Team` class.
- A reusable `Leaderboard<T>` that stores elements and a `Comparator<? super T>`.
- `add` and `addAll(List<? extends T>)` methods.
- TODO 1 completed by copying and sorting the stored items using the comparator.
- TODO 2 completed with `Comparator.comparingInt`, `reversed`, `thenComparing`, and a
  final alphabetical name comparison.
- Tests for the assignment's sample, multiple ties, unchanged insertion order, and a
  faulty subtraction comparator.

The correct comparator is:

```java
Comparator.comparingInt((Team team) -> team.points)
        .reversed()
        .thenComparing(
                Comparator.comparingInt((Team team) -> team.goalDiff).reversed())
        .thenComparing(team -> team.name);
```

## Algorithm

1. Copy the leaderboard's stored entries.
2. Sort the copy using the supplied comparator and return it.
3. Check the assignment sample against its required exact ranking.
4. Check a second dataset containing ties in points and goal difference against the
   expected alphabetical tie-break.
5. Test comparator reflexivity, sign symmetry, and transitivity over the tie dataset.
6. Use integer extrema to show that subtraction overflows and violates transitivity.
7. Write observed ranking rows and faulty-comparator outcomes to CSV.

## Source code references

- Generic leaderboard, comparator, and tests: [`LeaderboardDemo.java`](../Q5_GenericLeaderboard/LeaderboardDemo.java)
- Raw ranking results: [`q5_leaderboard_results.csv`](../experimental_data/q5_leaderboard_results.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q5_GenericLeaderboard\out | Out-Null
javac -Xlint:all --release 17 -d Q5_GenericLeaderboard\out Q5_GenericLeaderboard\LeaderboardDemo.java
java -cp Q5_GenericLeaderboard\out LeaderboardDemo
```

Compilation targeting Java 17 and execution succeeded on JDK 21.0.11 with no compiler
warnings.

## Expected output

The assignment ranking must be `Eagles, Bisons, Panthers, Cobras`; the tie example must
follow points descending, goal difference descending, and name ascending. The faulty
subtraction comparator should be shown to violate transitivity, and the correct checks
should pass. No timing result is expected because this question records correctness
evidence rather than a performance benchmark.

## Actual execution output evidence

Actual output:

```text
Assignment expected output:
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
Multiple-tie test:
[Bears(12,8), Hawks(12,8), Falcons(12,5), Wolves(12,5), Lions(10,5), Tigers(10,5)]
Faulty comparator pair signs (maximum/middle, middle/minimum, maximum/minimum): -1, -1, 1
Faulty comparator violates transitivity: true
Correct extreme-score ranking: [Maximum, Middle, Minimum]
Faulty sort output for this input: [Minimum, Maximum, Middle]
All correct-comparator checks passed.
Raw ranking results: experimental_data\q5_leaderboard_results.csv
```

The first ranking matches the assignment's expected output exactly. The program also
checks each expected ranking and throws an exception if the results differ.

## Experimental observations and comparison

| Test | Expected | Observed | Result |
|---|---|---|---|
| Assignment sample | Eagles, Bisons, Panthers, Cobras | Eagles, Bisons, Panthers, Cobras | Pass |
| Multiple ties | Bears, Hawks, Falcons, Wolves, Lions, Tigers | Bears, Hawks, Falcons, Wolves, Lions, Tigers | Pass |
| Correct comparator consistency | Reflexive, sign-symmetric, transitive on the test data | All checks passed | Pass |
| Faulty subtraction comparator | Maximum before Middle, Middle before Minimum, and Maximum before Minimum | Signs `-1`, `-1`, `1`; transitivity violated | Fault demonstrated |
| Faulty sort of extreme values | Maximum, Middle, Minimum | Minimum, Maximum, Middle | Incorrect ranking |

The CSV stores each correct ranking position and each faulty pair comparison. It is
correctness evidence, not a timing benchmark; Q5 does not require performance timing.

### Why the comparator is consistent

The comparator first compares points in descending order. If those are equal, it compares
goal difference in descending order. If both are equal, it compares names in ascending
lexicographic order. Each comparison is itself a consistent order, and `thenComparing`
uses the next comparison only when the earlier one returns zero. This lexicographic
composition is transitive. The final name comparison also ensures distinct teams with
the same numeric scores are not treated as equal by this ranking rule.

The faulty comparator uses `second.points - first.points`. With `Integer.MAX_VALUE`,
zero, and `Integer.MIN_VALUE`, subtraction overflows: it claims Maximum precedes Middle,
Middle precedes Minimum, but Maximum does not precede Minimum. The pairwise signs
demonstrate the contract violation independently of any one sorting implementation's
output.

## Time complexity

For `n` stored items:

- `add`: amortized `O(1)` for the backing `ArrayList`.
- `addAll`: `O(m)` for `m` incoming items.
- `ranking`: `O(n log n)` for sorting a copy.
- Comparator evaluation: `O(1)` for the three fields in this example.
- Comparator-law test: `O(n^3)`; used only on a small test dataset, not in normal
  leaderboard operations.

## Space complexity

The leaderboard stores `O(n)` references. Each `ranking()` call allocates an additional
`O(n)` list copy and the list sort may use temporary storage. The returned ranking is a
separate list, so callers may reorder it without changing the leaderboard's stored order.

## Alternative concept

If `Team` implemented `Comparable<Team>`, its `compareTo()` method would define one
default ranking and the leaderboard could sort using natural order. That couples the team
model to one ranking policy and is less convenient when other contexts need another order.
Separate comparators make the generic component reusable for teams, players, colleges,
or different ranking rules.

Sorting three times with stable sorts is another option: sort by name ascending, then goal
difference descending, then points descending. Stability preserves earlier tie-break order
when later sorts have equal keys. It requires three `O(n log n)` sorts, compared with one
sort using the comparator chain. The chain is more direct and less error-prone for this
three-field rule.

## Analysis and conclusion

The assignment output matched exactly. The tie dataset confirmed the points, goal
difference, and alphabetical rules at each level. Returning a sorted copy preserved
the original input order. The subtraction test exposed a real integer-overflow failure,
not merely an omitted tie-breaker.

The final design keeps ranking policy outside `Team` and inside the comparator passed to a
reusable generic leaderboard. This supports reuse while keeping the ranking logic explicit
and testable.

## Screenshot evidence

Capture the actual run manually as `output_screenshots/Q5/Q5_rankings_and_checks.png`.
See the [Q5 capture guide](../output_screenshots/Q5/README.md). No screenshot is included
yet.
