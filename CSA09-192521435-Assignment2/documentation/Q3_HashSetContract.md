# Q3 – Engineer the hashCode–equals Contract

## Aim

Investigate how the `equals()`/`hashCode()` contract affects duplicate student detection
in a `HashSet`, correct the supplied program, and observe what happens if a hash field
changes after insertion.

## Problem description

Students may be scanned at multiple event gates. A `HashSet<Student>` should retain one
record per registration number, even when separate `Student` objects represent the same
student.

## Objectives

- Compare the supplied `equals()`-only implementation before and after the hash-code fix.
- Run three kinds of experiments: same-reference rescanning, equal IDs in separate
  objects, and a mutable hash field.
- Predict the results and record the observed set results.
- Implement and verify the corrected student identity behavior.

## Concept explanation

For objects considered equal by `equals()`, Java requires their `hashCode()` values to be
equal. The reverse is not required: two unequal objects may have the same hash code.

A `HashSet` uses the hash code to select a bucket. It then uses equality checks among
entries in that bucket to decide whether an equal element is already present. If equal
objects return different hash codes, they can be routed to different buckets, and the set
may retain both.

Fields used by `equals()` and `hashCode()` should not change while an object is stored in
a hash-based collection. The object stays in the bucket selected at insertion time, while
later lookups calculate a hash from the changed field.

## Initial predictions

Before running the tests:

1. Adding the same object reference twice should leave one set element: the object has the
   same hash and is equal to itself.
2. Adding separate objects with the same registration number but different inherited
   identity hashes should leave two entries in the broken implementation. The experiment
   explicitly selects distinct inherited hash codes so the result does not depend on an
   accidental identity-hash collision.
3. The corrected implementation should keep one entry per registration number.
4. Changing a registration number used by `hashCode()` after insertion should make
   `contains()` fail for that object.

## Implementation approach

- [`AttendanceBefore.java`](../Q3_HashSetContract/AttendanceBefore.java) reproduces the
  supplied program before its TODO is completed. Its `StudentBefore` compares registration
  numbers but inherits `Object.hashCode()`.
- [`Attendance.java`](../Q3_HashSetContract/Attendance.java) completes TODO 1 by returning
  the registration number's hash code. It retains the supplied `Attendance` main program.
- [`HashSetExperiments.java`](../Q3_HashSetContract/HashSetExperiments.java) runs the
  designed experiments and supplied-program baseline, checks the results, and writes a
  CSV evidence file.

The corrected class keeps `regNo` final. This prevents the identity key from changing
while the student is in a set. The experiments also use a separate mutable test class to
demonstrate the consequence of changing the key.

## Algorithm

1. Re-run the supplied data before TODO 1 and record the broken program's set size.
2. Add one broken student reference twice and record the set size.
3. Add two separate broken student objects with equal registration numbers and distinct
   inherited hash codes; record both hash codes and the set size.
4. Add two unique and one duplicate student using the corrected equality/hash
   implementation; check that the size is two.
5. Add a mutable student to a set, confirm it is found, change its registration number,
   and confirm lookup fails.
6. Write the predictions and observations to CSV.

## Source code references

- Before TODO 1: [`AttendanceBefore.java`](../Q3_HashSetContract/AttendanceBefore.java)
- Completed supplied program: [`Attendance.java`](../Q3_HashSetContract/Attendance.java)
- Additional experiments: [`HashSetExperiments.java`](../Q3_HashSetContract/HashSetExperiments.java)
- Raw test results: [`q3_hashset_tests.csv`](../experimental_data/q3_hashset_tests.csv)

## Compilation and execution

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force Q3_HashSetContract\out | Out-Null
javac -Xlint:all --release 17 -d Q3_HashSetContract\out Q3_HashSetContract\AttendanceBefore.java Q3_HashSetContract\Attendance.java Q3_HashSetContract\HashSetExperiments.java
java -cp Q3_HashSetContract\out AttendanceBefore
java -cp Q3_HashSetContract\out Attendance
java -cp Q3_HashSetContract\out HashSetExperiments
```

The build and all three runs succeeded on JDK 21.0.11 with no compiler warnings.

## Expected output

The before-fix program should report three entries; the corrected program should report
two. The experiment should pass the same-reference, duplicate-instance, corrected
identity, and mutable-key checks. Identity hash values vary between JVM runs, so they are
not expected to match a stored number.

## Actual execution output evidence

Actual output from the run:

```text
--- Before completing TODO 1 ---
Unique students: 3
--- After completing TODO 1 ---
Unique students: 2
--- Additional HashSet experiments ---
Prediction before supplied program: set size=3
Supplied program before TODO: observed=3
Prediction before test 1: set size=1 (same reference)
Test 1 (same object scanned twice): observed=1
Prediction before test 2: set size=2 (equal IDs, different hashes)
Test 2 (equal IDs, separate objects): observed=2, hashes=2074407503/999966131
Prediction before test 3: set size=2 (registration number identity)
Test 3 (corrected Student): observed=2
Prediction before test 4: contains after key mutation=false
Test 4 (mutated hash field): contains before=true, after=false, hashes=822376264->822376551
Experimental results: experimental_data\q3_hashset_tests.csv
```

Identity hash values vary between runs. The CSV contains the values from the actual run;
the meaningful outcomes are the set sizes and lookup results.

## Experimental observations and measurements

| Test | Implementation and input | Predicted | Observed |
|---|---|---:|---:|
| Supplied program before TODO 1 | Original scan data with `equals()` only | Set size 3 | Set size 3 |
| Same reference scanned twice | `equals()` only; add the same object reference twice | Set size 1 | Set size 1 |
| Duplicate record objects | `equals()` only; equal registration numbers, distinct inherited hashes | Set size 2 | Set size 2 |
| Corrected duplicate records | `equals()` and matching `hashCode()`; two unique IDs and one duplicate | Set size 2 | Set size 2 |
| Mutable hash field | Add a student, change `regNo`, then call `contains()` | Lookup after change is false | Lookup after change is false |

The first case can appear to work because the same object reference has the same inherited
hash and is identical to itself. It does not show that two separately constructed but
equal students will be deduplicated.

## Time complexity

| Structure | Add/lookup per student | Notes |
|---|---|---|
| `HashSet<Student>` | Average `O(1)`; worst-case `O(n)` | Assumes a suitable hash distribution; computing a string hash depends on key length. |
| `TreeSet<Student>` ordered by registration number | `O(log n)` | Comparator must return zero exactly when student identity is equal. |
| `HashMap<String, Student>` keyed by registration number | Average `O(1)`; worst-case `O(n)` | The key itself is the unique identity; `putIfAbsent` can keep the first scan. |

## Space complexity

All three structures use `O(n)` space for `n` unique student records. A `HashSet` is
backed by a hash table; a `TreeSet` stores ordered tree nodes; and a `HashMap` stores
registration-number keys and student values in its table entries. Exact per-entry memory
depends on the JVM and collection implementation.

## Alternative concept

A `TreeSet<Student>` can use `Comparator.comparing(student -> student.regNo)`, with an
accessible registration-number accessor if the class is moved to its own file. It keeps
students ordered and detects duplicates when the comparator returns zero. A
`HashMap<String, Student>` can instead use the registration number directly as the key,
for example `putIfAbsent(regNo, student)`. The map key makes the de-duplication rule
explicit and avoids relying on `Student` identity for the collection key.

## Analysis and conclusion

The uncorrected supplied program printed three because separate `Student` instances
inherited identity-based hashes. Although their registration numbers compare equal, their
different hashes route them to different buckets, so the set does not use `equals()` to
deduplicate them.

Overriding `hashCode()` to use the same `regNo` as `equals()` fixes the contract, and the
completed program produced the required `Unique students: 2`. The mutation experiment
shows why the key fields should be immutable: after changing `regNo`, the lookup uses a
new hash and cannot find the object in its old bucket. If a mutable-key object must be
changed, remove it before changing the key and add it again afterward; an immutable
registration number is the safer model.

## Screenshot evidence

Capture the actual runs manually as `output_screenshots/Q3/Q3_before_and_after.png` and
`output_screenshots/Q3/Q3_hashset_experiments.png`. See the
[Q3 capture guide](../output_screenshots/Q3/README.md). No screenshots are included yet.
