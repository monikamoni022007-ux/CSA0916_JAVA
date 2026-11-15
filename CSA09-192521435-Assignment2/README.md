# Programming in Java – Assignment 2 (CO2)

## Student details

- Name: `kavyasree`
- Register number: `192524444`
- GitHub repository: `CSA09-192524444-Assignment2`
- Java version: JDK 17 or newer

Before submission, verify the student-name spelling and capitalization. See
[FINAL_SUBMISSION_CHECKLIST.md](FINAL_SUBMISSION_CHECKLIST.md) for the audit, required
manual screenshots, local Git commit commands, and remaining confirmation steps.

## Project structure

```text
JAVA_ASSIGNMENT_2/
├── ASSIGNMENT2.pdf
├── README.md
├── Q1_StringComparison/
├── Q2_ListComparison/
├── Q3_HashSetContract/
├── Q4_MapComparison/
├── Q5_GenericLeaderboard/
├── Q6_HospitalTriage/
├── Q7_DesignChallenge/
├── experimental_data/
├── output_screenshots/
│   ├── Q1/ ... Q7/ (capture guides; add genuine screenshots manually)
└── documentation/
```

Each question folder will contain its Java source code. Raw measurements will be saved
as CSV files in `experimental_data/`, question write-ups are in `documentation/`, and
screenshots must be captured from actual program runs and saved in the corresponding
`output_screenshots/Q1/` through `output_screenshots/Q7/` folder. Each screenshot folder
contains a capture guide; the guides are not screenshots or execution evidence.

## Compile and run

Install JDK 17 or newer and confirm that `java` and `javac` are available in the terminal:

```powershell
java -version
javac -version
```

For Q1, run the following commands from the project root. The program writes its CSV
measurements to `experimental_data/` relative to the current directory:

```powershell
New-Item -ItemType Directory -Force Q1_StringComparison\out | Out-Null
javac --release 17 -d Q1_StringComparison\out Q1_StringComparison\TextBuildingBenchmark.java
java -cp Q1_StringComparison\out TextBuildingBenchmark
```

Q2 can be compiled and run from the project root with:

```powershell
New-Item -ItemType Directory -Force Q2_ListComparison\out | Out-Null
javac --release 17 -d Q2_ListComparison\out Q2_ListComparison\PlaylistBenchmark.java
java -cp Q2_ListComparison\out PlaylistBenchmark
```

Q3 includes the uncorrected and corrected supplied program plus the additional tests:

```powershell
New-Item -ItemType Directory -Force Q3_HashSetContract\out | Out-Null
javac --release 17 -d Q3_HashSetContract\out Q3_HashSetContract\AttendanceBefore.java Q3_HashSetContract\Attendance.java Q3_HashSetContract\HashSetExperiments.java
java -cp Q3_HashSetContract\out AttendanceBefore
java -cp Q3_HashSetContract\out Attendance
java -cp Q3_HashSetContract\out HashSetExperiments
```

Q4 can be compiled and run from the project root with:

```powershell
New-Item -ItemType Directory -Force Q4_MapComparison\out | Out-Null
javac --release 17 -d Q4_MapComparison\out Q4_MapComparison\PharmacyInventoryBenchmark.java
java -cp Q4_MapComparison\out PharmacyInventoryBenchmark
```

Q5 can be compiled and run from the project root with:

```powershell
New-Item -ItemType Directory -Force Q5_GenericLeaderboard\out | Out-Null
javac --release 17 -d Q5_GenericLeaderboard\out Q5_GenericLeaderboard\LeaderboardDemo.java
java -cp Q5_GenericLeaderboard\out LeaderboardDemo
```

Q6 can be compiled and run from the project root with:

```powershell
New-Item -ItemType Directory -Force Q6_HospitalTriage\out | Out-Null
javac --release 17 -d Q6_HospitalTriage\out Q6_HospitalTriage\Patient.java Q6_HospitalTriage\Triage.java Q6_HospitalTriage\TriageExperiments.java
java -cp Q6_HospitalTriage\out Triage
java -cp Q6_HospitalTriage\out TriageExperiments
```

Q7 can be compiled and run from the project root with:

```powershell
New-Item -ItemType Directory -Force Q7_DesignChallenge\out | Out-Null
javac --release 17 -d Q7_DesignChallenge\out Q7_DesignChallenge\DesignChallenge.java
java -cp Q7_DesignChallenge\out DesignChallenge
```

## Questions

1. **Q1 – String, StringBuilder and StringBuffer:** Compare ticket-building approaches
   under different workloads, measure repeated runs, and recommend choices for tickets
   and shared logs.
2. **Q2 – ArrayList, LinkedList and ArrayDeque:** Measure playlist operations at
   different sizes and evaluate the Up Next queue.
3. **Q3 – HashSet and the hashCode–equals contract:** Test duplicate detection,
   complete the supplied program, and explain how mutable hash fields affect a set.
4. **Q4 – HashMap, TreeMap and LinkedHashMap:** Compare pharmacy inventory lookup and
   report generation, including ordering and a sorted-array alternative.
5. **Q5 – Generics, Comparable and Comparator:** Build and test a reusable generic
   leaderboard with consistent multi-field ranking.
6. **Q6 – Queue and PriorityQueue:** Implement hospital triage with severity priority,
   FIFO ties, performance measurements, and a discussion of starvation and fairness.
7. **Q7 – Diagnose, Redesign and Validate:** Use measured evidence from Q1–Q6 to choose,
   redesign, and compare one solution.

## Results summary

This table is updated as each question is implemented and executed. Measurements and
conclusions are based on observed runs, not estimates.

| Question | Execution and measurements | Summary |
|---|---|---|
| Q1 | Compiled with `--release 17`; ran 5 measured repetitions per approach and workload. CSVs: `experimental_data/q1_string_timing.csv`, `experimental_data/q1_string_memory.csv`. | `String +` was fastest for the six-field workload; mutable builders were substantially faster for 50- and 200-field tickets. See [Q1 documentation](documentation/Q1_StringComparison.md). |
| Q2 | Compiled with `--release 17`; ran 5 measured repetitions for 7 operations, 3 structures, and 3 sizes. CSV: `experimental_data/q2_list_benchmark.csv`. | `ArrayList` led for indexed reads and measured middle edits; `ArrayDeque` fits the Up Next end-operation pattern. See [Q2 documentation](documentation/Q2_ListComparison.md). |
| Q3 | Compiled with `--release 17`; executed before/after TODO and ran four contract tests. CSV: `experimental_data/q3_hashset_tests.csv`. | The uncorrected program retained 3 entries; after overriding `hashCode()` it retained 2. Mutating a hashed identity field made lookup fail. See [Q3 documentation](documentation/Q3_HashSetContract.md). |
| Q4 | Compiled with `--release 17`; measured build, 100,000 lookups, and two reports across 3 sizes with 5 repetitions. CSV: `experimental_data/q4_map_benchmark.csv`. | `HashMap` and `LinkedHashMap` had similar lookup times at 50,000 items; `LinkedHashMap` preserves insertion order and had lower combined report time in this run. See [Q4 documentation](documentation/Q4_MapComparison.md). |
| Q5 | Compiled with `--release 17`; checked the supplied output, a multiple-tie dataset, comparator laws, and a subtraction-overflow comparator. CSV: `experimental_data/q5_leaderboard_results.csv`. | The supplied output matched exactly; the deliberately faulty comparator violated transitivity for integer extremes. See [Q5 documentation](documentation/Q5_GenericLeaderboard.md). |
| Q6 | Compiled with `--release 17`; verified the supplied expected output and eight-patient order; measured 5 repeated admission and service batches at 3 sizes. CSVs: `experimental_data/q6_triage_results.csv`, `experimental_data/q6_triage_benchmark.csv`. | Severity and FIFO tie ordering passed; 50,000 patients were served in an average 23.202 ms in this run. See [Q6 documentation](documentation/Q6_HospitalTriage.md). |
| Q7 | Compiled with `--release 17`; ran two warm-ups and 10 measured comparisons on 10,000 tickets with 50 fields. CSVs: `experimental_data/q7_design_timing.csv`, `experimental_data/q7_design_memory.csv`. | Pre-sized `StringBuilder` reduced mean elapsed time by 87.5% in this run and matched output checksums. The approximate heap sample is higher for the builder and does not establish memory improvement. See [Q7 documentation](documentation/Q7_DesignChallenge.md). |

## GitHub submission

The intended repository name is `CSA09-192524444-Assignment2`. Create and share the
repository as required by the course. No repository has been created, configured, or
pushed by this project setup; see the checklist for local Git instructions. Do not push
without explicit authorization.
