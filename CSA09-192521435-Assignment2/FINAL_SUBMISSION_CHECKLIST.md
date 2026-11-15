# Java Assignment 2 â€“ Final Submission Checklist

## Status

**Not submission-ready yet.** The source code, question write-ups, CSV datasets, and
Java 17-targeted compilation are present. Manual screenshots, student details, repository
setup, and final confirmation against the instructor's complete submission instructions
are still outstanding. Do not mark the assignment complete until those items are checked.

## Audit completed

| Requirement | Current evidence | Status |
|---|---|---|
| Q1â€“Q7 Java sources | Source files are present in each question folder; Q3 and Q6 include their supporting model/experiment files. | Verified |
| Per-question documentation | Seven question write-ups exist in `documentation/`. | Verified |
| Experimental CSVs | Q1: 2; Q2: 1; Q3: 1; Q4: 1; Q5: 1; Q6: 2; Q7: 2. | Verified |
| Compile/run instructions | Project-root PowerShell compile and run instructions appear in the README and question write-ups. | Verified |
| Expected-output guidance | Every question write-up now separates expected output/format from its recorded actual run evidence. Timings and identity hashes are correctly described as variable. | Verified |
| Actual output/measurement evidence | Each question write-up contains recorded output and/or measurements; CSV files hold raw run data where applicable. These are existing run records, not newly generated screenshots. | Present; manually capture screenshots below |
| Java 17 compilation | All seven question source sets compiled with `javac -Xlint:all --release 17` without errors or warnings during this audit. | Verified |
| Q7 CSV-derived result | Ten samples per approach; mean `String +` 39.504 ms and pre-sized `StringBuilder` 7.173 ms; the reduction is approximately 81.84%, rounded by the program to 81.8%. Checksums match across the two approaches in every run. | Verified |
| Q7 retained heap | The single post-GC samples are 8,200,608 bytes (`String +`) and 9,369,704 bytes (builder). This does **not** establish memory improvement; the builder sample is higher. | Correctly qualified |
| Screenshot folders | `output_screenshots/Q1` through `Q7` contain capture guides. Q1 has one earlier terminal image and Q2 has three earlier terminal images; their timings predate the latest CSVs. Q3â€“Q7 have no screenshot images. | Recapture Q1â€“Q2 against the latest runs; capture Q3â€“Q7 |
| Student information | Name `kavyasree` and register number `192524444` are entered in `README.md`. | Entered; verify spelling/capitalization |
| Git repository | This directory is not currently a Git repository. The intended remote repository has not been created or configured. | Remaining |
| Push to GitHub | No push has been performed. | Requires your permission |
| Official submission requirements | `ASSIGNMENT2.pdf` is included, but its complete text/rubric could not be verified in this workspace. Check the original brief/LMS for required format, report, screenshots, and submission method. | Needs manual confirmation |
| One consolidated academic report | The project has seven detailed question write-ups and a README index, not one merged report/PDF. The project README describes question-wise write-ups; confirm whether the instructor separately requires one combined report before making a duplicate. | Confirm against official brief |

## Remaining actions â€” do these in order

### 1. Supply student details

- [x] Enter student name `kavyasree`; verify its requested capitalization/spelling.
- [x] Enter register number `192524444`; the proposed repository name is
  `CSA09-192524444-Assignment2`.
- [ ] Check that no other title page or report field requires student details.

### 2. Confirm the official brief and report format

- [ ] Compare this checklist with the complete `ASSIGNMENT2.pdf` and the instructor/LMS
  submission instructions.
- [ ] Confirm whether the seven individual question write-ups are acceptable or whether
  one combined academic report/PDF is mandatory. Do not merge and duplicate the seven
  reports unless required.
- [ ] Confirm whether additional cover-page, citation, formatting, or submission-platform
  rules apply.

### 3. Capture real terminal screenshots

Use the VS Code terminal from the project root. Run the exact commands in the next
section, keep the command and relevant output visible, and save actual screenshots using
the filenames below. Do not create mock output, edit timings, or use these Markdown
guides as screenshot evidence. If output does not fit legibly, use consecutively numbered
`_part1`, `_part2` files.

| Question | Manually capture | Recommended filename(s) |
|---|---|---|
| Q1 | Java invocation; `StringJoiner` and `String.format` examples; latest workload averages; equivalence confirmation; CSV paths. | `output_screenshots/Q1/Q1_run_latest.png` |
| Q2 | Up Next example and validation; full measured-operation summary; repetition counts and CSV path. | `output_screenshots/Q2/Q2_run_latest_part1.png`, `output_screenshots/Q2/Q2_run_latest_part2.png`, `output_screenshots/Q2/Q2_run_latest_part3.png` |
| Q3 | Before/after unique-student counts; experiment outcomes, mutable-key result, CSV path. | `output_screenshots/Q3/Q3_before_and_after.png`, `output_screenshots/Q3/Q3_hashset_experiments.png` |
| Q4 | Order-verification messages for every approach/size; measured benchmark summary and CSV path. | `output_screenshots/Q4/Q4_order_validation.png`, `output_screenshots/Q4/Q4_benchmark_summary.png` |
| Q5 | Assignment and tie rankings; faulty-comparator demonstration; passing checks and CSV path. | `output_screenshots/Q5/Q5_rankings_and_checks.png` |
| Q6 | Supplied six-patient order; eight-patient validation; benchmark summary and CSV paths. | `output_screenshots/Q6/Q6_supplied_triage_order.png`, `output_screenshots/Q6/Q6_eight_patient_validation.png`, `output_screenshots/Q6/Q6_benchmark_summary.png` |
| Q7 | Output equivalence/checksum; measured averages and 81.8% reduction; objective status; both heap deltas and caveat. | `output_screenshots/Q7/Q7_equivalence_and_timing.png`, `output_screenshots/Q7/Q7_heap_sample_caveat.png` |

Each screenshot folder's `README.md` gives capture-specific details.

### 4. Prepare Git history

The following are PowerShell commands to run from the project root **after** filling in
the register number and capturing screenshots. They create local commits only. They do
not create a GitHub repository or push anything. Replace the placeholder in the remote
URL only after creating the repository on GitHub.

```powershell
git init -b main
git add Q1_StringComparison documentation/Q1_StringComparison.md experimental_data/q1_string_timing.csv experimental_data/q1_string_memory.csv output_screenshots/Q1
git commit -m "Complete Q1 string building comparison"

git add Q2_ListComparison documentation/Q2_ListComparison.md experimental_data/q2_list_benchmark.csv output_screenshots/Q2
git commit -m "Complete Q2 collection comparison"

git add Q3_HashSetContract documentation/Q3_HashSetContract.md experimental_data/q3_hashset_tests.csv output_screenshots/Q3
git commit -m "Complete Q3 HashSet contract experiments"

git add Q4_MapComparison documentation/Q4_MapComparison.md experimental_data/q4_map_benchmark.csv output_screenshots/Q4
git commit -m "Complete Q4 map comparison"

git add Q5_GenericLeaderboard documentation/Q5_GenericLeaderboard.md experimental_data/q5_leaderboard_results.csv output_screenshots/Q5
git commit -m "Complete Q5 generic leaderboard"

git add Q6_HospitalTriage documentation/Q6_HospitalTriage.md experimental_data/q6_triage_results.csv experimental_data/q6_triage_benchmark.csv output_screenshots/Q6
git commit -m "Complete Q6 hospital triage"

git add Q7_DesignChallenge documentation/Q7_DesignChallenge.md experimental_data/q7_design_timing.csv experimental_data/q7_design_memory.csv output_screenshots/Q7
git commit -m "Complete Q7 redesign and validation"

git add .gitignore ASSIGNMENT2.pdf README.md FINAL_SUBMISSION_CHECKLIST.md output_screenshots
git commit -m "Prepare final submission documentation"
```

Before any remote operation, review the local history and staged/unstaged files:

```powershell
git status --short
git log --oneline --decorate -8
```

After you explicitly authorize a push and create the remote repository, configure it
using its actual URL:

```powershell
git remote add origin https://github.com/<GitHubUser>/CSA09-192524444-Assignment2.git
git remote -v
git push -u origin main
```

**Do not run the remote commands without your permission.** No remote URL is assumed.

### 5. Final pre-submission verification

- [ ] All listed screenshot files are present and show genuine terminal output.
- [ ] Student information is complete and consistent.
- [ ] Official brief requirements and report format have been confirmed.
- [ ] Q1â€“Q7 CSVs and documentation are included in their corresponding commits.
- [ ] `git status --short` shows no unintended or missing changes.
- [ ] Local commit history has seven separate Q1â€“Q7 commits.
- [ ] Any GitHub repository creation/push has been explicitly authorized and completed
  only if required.

## Exact Java commands and expected output guide

Run all commands from the project root. Compile commands use Java 17 language/API
compatibility. The Java commands below are the exact execution commands for the
documented programs.

### Q1 â€” Text building

```powershell
New-Item -ItemType Directory -Force Q1_StringComparison\out | Out-Null
javac -Xlint:all --release 17 -d Q1_StringComparison\out Q1_StringComparison\TextBuildingBenchmark.java
java -cp Q1_StringComparison\out TextBuildingBenchmark
```

Expected format: alternative-format examples; 3 approaches Ã— 3 workload timing lines;
warm-up/repetition counts; identical-output confirmation; timing and memory CSV paths.
Numbers vary. Actual recorded values and output are in
[Q1 documentation](documentation/Q1_StringComparison.md).

### Q2 â€” Lists and deque

```powershell
New-Item -ItemType Directory -Force Q2_ListComparison\out | Out-Null
javac -Xlint:all --release 17 -d Q2_ListComparison\out Q2_ListComparison\PlaylistBenchmark.java
java -cp Q2_ListComparison\out PlaylistBenchmark
```

Expected deterministic lines: `Up Next first: Play Next`,
`Up Next remaining: Track A -> Track B`, and
`ArrayList, LinkedList, and ArrayDeque operation checks passed.` A 7-operation Ã—
3-size Ã— 3-structure timing table and CSV path follow; timings vary. See
[Q2 documentation](documentation/Q2_ListComparison.md).

### Q3 â€” HashSet contract

```powershell
New-Item -ItemType Directory -Force Q3_HashSetContract\out | Out-Null
javac -Xlint:all --release 17 -d Q3_HashSetContract\out Q3_HashSetContract\AttendanceBefore.java Q3_HashSetContract\Attendance.java Q3_HashSetContract\HashSetExperiments.java
java -cp Q3_HashSetContract\out AttendanceBefore
java -cp Q3_HashSetContract\out Attendance
java -cp Q3_HashSetContract\out HashSetExperiments
```

Expected key results: before fix `Unique students: 3`; corrected program
`Unique students: 2`; experiments report sizes `1`, `2`, `2` and a failed lookup after
the mutable identity key changes. Inherited hash values vary. See
[Q3 documentation](documentation/Q3_HashSetContract.md).

### Q4 â€” Maps and sorted array

```powershell
New-Item -ItemType Directory -Force Q4_MapComparison\out | Out-Null
javac -Xlint:all --release 17 -d Q4_MapComparison\out Q4_MapComparison\PharmacyInventoryBenchmark.java
java -cp Q4_MapComparison\out PharmacyInventoryBenchmark
```

Expected format: successful sorted-code and insertion-order verification for all four
approaches at 100, 10,000, and 50,000 entries; build/lookup/report timings; repetition
counts and CSV path. Timings vary. See
[Q4 documentation](documentation/Q4_MapComparison.md).

### Q5 â€” Generic leaderboard

```powershell
New-Item -ItemType Directory -Force Q5_GenericLeaderboard\out | Out-Null
javac -Xlint:all --release 17 -d Q5_GenericLeaderboard\out Q5_GenericLeaderboard\LeaderboardDemo.java
java -cp Q5_GenericLeaderboard\out LeaderboardDemo
```

Expected assignment ranking: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`;
tie-rule and faulty-comparator tests should run and the correct-comparator checks should
pass. See [Q5 documentation](documentation/Q5_GenericLeaderboard.md).

### Q6 â€” Hospital triage

```powershell
New-Item -ItemType Directory -Force Q6_HospitalTriage\out | Out-Null
javac -Xlint:all --release 17 -d Q6_HospitalTriage\out Q6_HospitalTriage\Patient.java Q6_HospitalTriage\Triage.java Q6_HospitalTriage\TriageExperiments.java
java -cp Q6_HospitalTriage\out Triage
java -cp Q6_HospitalTriage\out TriageExperiments
```

Expected supplied order:
`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`. The experiment should pass
its eight-patient ordering and print admit/serve timing rows and CSV paths. Timing values
vary. See [Q6 documentation](documentation/Q6_HospitalTriage.md).

### Q7 â€” Diagnose, redesign, validate

```powershell
New-Item -ItemType Directory -Force Q7_DesignChallenge\out | Out-Null
javac -Xlint:all --release 17 -d Q7_DesignChallenge\out Q7_DesignChallenge\DesignChallenge.java
java -cp Q7_DesignChallenge\out DesignChallenge
```

Expected: equivalent output/checksums, ten samples per approach, computed time reduction
and objective status, plus both approximate retained-heap samples with a caution. The
audited CSV supports 39.504 ms vs. 7.173 ms, an approximately 81.84% reduction
rounded by the program to 81.8%.
The heap sample is higher for the builder and does not establish memory improvement.
Current recorded output is in [Q7 documentation](documentation/Q7_DesignChallenge.md).

