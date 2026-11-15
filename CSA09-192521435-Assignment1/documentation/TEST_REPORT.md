# Test Report

## Build

Command run from `C:\Users\KAVYA SREE\JAVA 1`:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java |
    Where-Object { $_.FullName -notmatch '\\(out|build)\\' } |
    Select-Object -ExpandProperty FullName
javac -Xlint:all -d out $files
```

Actual result: **34 Java source files compiled successfully with no warnings.**

## Task programs

Each task entry point `task01.ProblemAnalysis` through `task12.Day4DesignChallenge` was executed using `java -cp out <fully-qualified-class>`. Every process exited successfully.

| Test ID | Task | Scenario | Expected | Actual | Status |
| --- | --- | --- | --- | --- | --- |
| T03-01 | 3 | Allocate 3 slots across floors of sizes 2 and 1 | `[0,0]`, `[0,1]`, `[1,0]` | `[0, 0]`, `[0, 1]`, `[1, 0]` | PASS |
| T03-02 | 3 | Allocate when full | `null` | `null` | PASS |
| T03-03 | 3 | Release same slot twice and release invalid floor | `true false false` | `true false false` | PASS |
| T03-04 | 3 | Reallocate released slot | `[0,1]` | `[0, 1]` | PASS |
| T08-01 | 8 | Two-wheeler, 130 minutes | Rs 30.0 | Rs 30.0 | PASS |
| T08-02 | 8 | Car, 130 minutes | Rs 90.0 | Rs 90.0 | PASS |
| T08-03 | 8 | EV, 130 minutes plus 10 kWh | Rs 210.0 | Rs 210.0 | PASS |
| T08-04 | 8 | Car and EV with same registration compare equal by registration | `true` | `true` | PASS |

Task 3 exact output:

```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

Task 8 exact output:

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

## Integrated acceptance tests

Command: `java -cp out campusparking.app.AcceptanceTests`
Actual result: **38 checks passed**.

| Test ID range | Scenario | Expected | Actual | Status |
| --- | --- | --- | --- | --- |
| T11-01–06 | Two-wheeler and car 0/1/60/61/130-minute boundaries | zero is free; each started hour billed; 130-minute rates match Task 8 | Rs 0, 10, 10, 20, 30 and car Rs 90 | PASS |
| T11-07–10 | Negative duration, blank registration, unknown type, e-bike without configured fee | Reject each invalid input | All rejected with exceptions | PASS |
| T11-11–13 | First allocation, next unequal floor, unique ticket IDs | First slot chosen; five-floor capacities can differ; IDs differ | All conditions true | PASS |
| T11-14–21 | Duplicate/full facility/unknown exit/early exit/retained ticket/slot reuse/invalid slot/double occupancy | Reject invalid operations without losing active state | All conditions verified | PASS |
| T11-22–28 | Monthly pass, visitor pass, expiry, EV charging and negative kWh | Monthly parking waived; visitor still pays; expired pass and negative energy rejected | Rs 0.0, Rs 30.0, Rs 120.0 and expected rejections | PASS |
| T12-01–03 | E-bike configured flat fee for short and long stay, plus factory normalization | Return injected test fixture fee; hyphenated forms accepted | Rs 17.50 for each and successful factory acceptance | PASS |
| T12-04–10 | Delivery van before/at/through/after 09:00–17:00, partial overlap, across midnight | Apply Rs 30/hour off peak and Rs 60/hour peak proportionally | Rs 30, 60, 60, 30, 45, 510 and 600 | PASS |
| T11-37 | Hand-calculated daily collection | Rs 90 + Rs 10 + Rs 45 + Rs 210 = Rs 355 | Rs 355.0 | PASS |

Task 12 entry point: `java -cp out task12.Day4DesignChallenge`
Actual result: **19 challenge checks passed**.

## Integrated application

Command: `java -cp out campusparking.app.Main`

Actual output:

```text
Initial entry complete. Active vehicles: 2
Car fee: 0.0
Delivery van fee: 502.5
```

## Lookup timing experiment

Command: `java -cp out campusparking.app.LookupBenchmark`

One actual run (10,000 entries, 10,000 searches for the last entry):

```text
Array linear scan: 570.7943 ms
ArrayList.indexOf: 586.3727 ms
HashMap.get: 0.8177 ms
Checksums equal: true
```

This is one local timing run, not a statistically controlled benchmark; timings vary by JVM, warmup and machine. The checksums confirm all structures returned the same target index.

## Screenshots and evidence

No screenshots were captured or fabricated. Use [SCREENSHOT_CHECKLIST.md](<C:/Users/KAVYA SREE/JAVA 1/documentation/SCREENSHOT_CHECKLIST.md>) to capture genuine terminal evidence in VS Code.
