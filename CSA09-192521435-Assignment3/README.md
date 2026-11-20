# Programming in Java (CSA09) – Assignment 3 (CO3)
## Exception Handling, Resource Safety, Multithreading & Concurrent Programming

**Course:** Programming in Java (CSA09)  
**Course Outcome Assessed:** CO3 (Sessions 27–38)  
**Bloom's Taxonomy Levels:** K4 (Analyse - Q1 to Q6) & K5 (Evaluate - Q7)  
**Total Marks:** 50 Marks  
**Runtime Environment:** OpenJDK `21.0.11` LTS (Temurin-21.0.11+10, 64-Bit Server VM)  
**Execution Mode:** Pair Work – Student A and Student B  

---

## 1. Repository Directory Structure
```text
c:\Users\KAVYA SREE\JAVA 3/
├── README.md                            # Master repository documentation
├── ASSIGNMENT3.pdf                      # Official assignment specification
├── Q1/                                  # Q1: ATM Exception Handling (6 Marks)
│   ├── AtmSimulation.java               # Main simulation (try, catch, multi-catch, finally)
│   ├── CatchOrderCompilerErrorDemo.java # Student B: Catch block reachability demonstration
│   ├── AlternativeErrorCodeAtm.java     # Alternative Concept: Pre-checks & error codes
│   ├── UncaughtExceptionDemo.java       # Student B: Uncaught exception stack trace
│   ├── card_reader.cfg                  # Hardware card reader config
│   └── README.md                        # Task 01 documentation
├── Q2/                                  # Q2: User-Defined Exceptions & Propagation (7 Marks)
│   ├── StorageDemo.java                 # Complete the Code (TODO 1 Solved)
│   ├── OnlineExamPortal.java            # Three-Layer Architecture (Controller, Service, Storage)
│   ├── UnchainedExceptionDemo.java      # Student B: Diagnostic loss without chaining
│   ├── UncheckedAlternativeDemo.java    # Alternative Concept: RuntimeException evaluation
│   └── README.md                        # Task 02 documentation
├── Q3/                                  # Q3: Resource Safety & Try-With-Resources (7 Marks)
│   ├── ManualSalesReportGenerator.java  # Leaky manual close vs manual safe finally
│   ├── SafeReportGenerator.java         # Robust try-with-resources with logging
│   ├── SuppressedExceptionDemo.java     # Student B: Throwable.getSuppressed() demonstration
│   ├── ReportEquivalenceVerification.java # Shared Work: Equivalence check
│   ├── data/                            # Store sales test data (store1, store2_corrupt, store3)
│   └── README.md                        # Task 03 documentation
├── Q4/                                  # Q4: Thread Creation & Life Cycle (7 Marks)
│   ├── ParallelScannerSimulation.java   # Dual implementation (extends Thread & implements Runnable)
│   ├── RunVsStartDemo.java              # Student A: start() vs run() experimental proof
│   ├── ThreadLifecycleStateTracker.java # Student A: Tracing all 6 Thread States
│   ├── StudentBExperiments.java         # Student B: 10 Runs, Priorities & Daemon tests
│   ├── ExecutorAlternativeScanner.java  # Alternative Concept: Fixed Thread Pool
│   └── README.md                        # Task 04 documentation
├── Q5/                                  # Q5: Race Conditions in Seat-Booking (6 Marks)
│   ├── SeatBooking.java                 # Complete the Code (TODO 1 Solved)
│   ├── UnsynchronizedSeatBooking.java   # PDF Requirement: 10 Unsynchronized Runs
│   ├── CinemaSyncComparison.java        # Student B: 10,000 Bookings Benchmark
│   ├── AlternativeBookingTechniques.java # Alternative Concepts: AtomicBoolean, Lock, Map
│   └── README.md                        # Task 05 documentation
├── Q6/                                  # Q6: Deadlock & Inter-Thread Communication (7 Marks)
│   ├── TokenCounter.java                # Complete the Code (TODO 1 & 2 Solved)
│   ├── IfVsWhileTokenCounterDemo.java   # PDF Requirement: while vs. if experiment
│   ├── HostelWalletDeadlockDemo.java    # Safe Controlled Deadlock Demonstration
│   ├── HostelWalletSafeTransfer.java    # Deadlock-Free Ordered Locking
│   ├── BoundedBufferTracingDemo.java    # Student A: Bounded buffer (0 <= count <= 5) tracing
│   ├── AlternativeQueueAndTryLock.java  # Alternative Concepts: BlockingQueue & tryLock
│   └── README.md                        # Task 06 documentation
└── Q7/                                  # Q7: Fault-Tolerant Processor (10 Marks)
    ├── SemesterResultProcessor.java     # Main processor, hand-checked set & 10,000 benchmark
    ├── ParallelStreamAlternative.java   # Alternative Concept: Parallel streams & memory
    └── README.md                        # Complete 4-stage technical documentation
```

Compiled classes, execution logs, and generated reports are excluded from Git.
Run the commands in each question's README to regenerate them locally.

---

## 2. Complete the Code Solutions Summary
Every "Complete the Code" program from the assignment PDF has been completed, tested, and verified:
1. **Q2 (`StorageDemo.java` – TODO 1):**
   ```java
   throw new SubmissionStorageException("Could not store " + fileName, e);
   ```
   **Output:** `Saved answer01.pdf` | `Error: Could not store answer02.pdf` | `Cause: Disk quota exceeded`
2. **Q5 (`SeatBooking.java` – TODO 1):**
   ```java
   public synchronized void book(int seat) {
       if (!booked[seat]) {
           booked[seat] = true;
           bookingsMade++;
       }
   }
   ```
   **Output:** `Seats sold: 10`
3. **Q6 (`TokenCounter.java` – TODO 1 & 2):**
   - **TODO 1 (`put`):** Guarded `while (count == buffer.length) { wait(); }`, insert, `notifyAll()`.
   - **TODO 2 (`take`):** Guarded `while (count == 0) { wait(); }`, extract, `notifyAll()`.
   **Output:** `Sum of tokens collected: 210 (expected 210)`

---

## 3. End-to-End Verification Audit (All 7 Questions)

| Question | Assessed Area | Source Files | Compilation | Execution Status | Test Results Summary | Missing Requirements |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **Q1** | ATM Exception Handling (try/catch/finally) | 4 | **PASS** | **PASS** | 5 test scenarios verified; card always ejected | None |
| **Q2** | User-Defined Exceptions & Chaining | 4 | **PASS** | **PASS** | TODO 1 exact match; 3 layers verified | None |
| **Q3** | Resource Safety & Try-With-Resources | 4 | **PASS** | **PASS** | 2 leaks in manual vs 0 in TWR; Grand Total: Rs. 7000 | None |
| **Q4** | Thread Creation & Life Cycle | 5 | **PASS** | **PASS** | 100 scripts scanned; 10 runs & daemon verified | None |
| **Q5** | Race Conditions in Seat Booking | 4 | **PASS** | **PASS** | TODO 1 exact match; 10 unsync runs & 10k test | None |
| **Q6** | Deadlock & Producer-Consumer Counter | 6 | **PASS** | **PASS** | TODO 1&2 exact match; safe controlled deadlock | None |
| **Q7** | Fault-Tolerant Processor (10,000 files) | 2 | **PASS** | **PASS** | Verified pools 1, 2, 4, 8; 100% completeness | None |

---

## 4. Q7 Benchmark Results (10,000 Files Across Thread Pools 1, 2, 4, 8)
- **Dataset:** 10,000 total files ($9,960\text{ valid} + 20\text{ corrupt non-numeric} + 20\text{ missing}$).
- **Completeness:** $9,960\text{ Success} + 40\text{ Failed} = 10,000$ ($100\%$ accounted for; 0 thread crashes).
- **Execution Measurements:**
  - **1 Thread (Sequential Baseline):** $5,831\text{ ms}$ ($1.00\times$)
  - **2 Threads:** $3,539\text{ ms}$ ($1.65\times$ speedup)
  - **4 Threads:** $2,750\text{ ms}$ ($2.12\times$ speedup)
  - **8 Threads:** $2,386\text{ ms}$ ($2.44\times$ speedup)

---

## 5. How to Compile and Execute Each Task

```bash
# Question 1: ATM Simulation
cd Q1
javac *.java
java AtmSimulation

# Question 2: Online Exam Portal
cd ../Q2
javac *.java
java StorageDemo
java OnlineExamPortal

# Question 3: Resource Safety & Daily Sales Report
cd ../Q3
javac *.java
java SafeReportGenerator
java SuppressedExceptionDemo
java ReportEquivalenceVerification

# Question 4: Answer-Script Scanner Thread Lifecycle
cd ../Q4
javac *.java
java ParallelScannerSimulation
java RunVsStartDemo
java StudentBExperiments

# Question 5: Cinema Seat Booking Race Conditions
cd ../Q5
javac *.java
java SeatBooking
java UnsynchronizedSeatBooking
java CinemaSyncComparison

# Question 6: Deadlock & Mess Token Counter
cd ../Q6
javac *.java
java TokenCounter
java IfVsWhileTokenCounterDemo
java HostelWalletSafeTransfer
java HostelWalletDeadlockDemo

# Question 7: Fault-Tolerant Processor (10,000 Files)
cd ../Q7
javac *.java
java SemesterResultProcessor
java ParallelStreamAlternative
```

---

## 6. Mandatory Individual Contribution Record (PDF Page 11)

| Question | Student A – Primary Responsibility | Student B – Primary Responsibility | Joint Contribution |
| :---: | :--- | :--- | :--- |
| **Q1** | Exception Design, Throwable Hierarchy & Catch Order | 5 Test Scenarios, Stack Trace & Broader Catch Demo | ATM Code Implementation & Error-Code Analysis |
| **Q2** | Exception Hierarchy Diagram & Propagation Path | Failure Scenarios, Chaining Evidence & Unchained Demo | TODO 1 Solution & RuntimeException Analysis |
| **Q3** | Breakpoint Step-Through & Open Handle Tracking | Resource Scope, Reverse Closing & Suppressed Exceptions | Manual/TWR Implementations & $O(f)$ Analysis |
| **Q4** | Thread vs. Runnable, Life-Cycle & `start()` vs `run()` | 10 Runs Order, Priority & Daemon Thread Experiments | Scanner Stations Code & Thread Pool Sizing |
| **Q5** | Race Reproduction, Interleaving & Critical Section | 10,000 Bookings Benchmark & Intrinsic Monitor Analysis | TODO 1 Solution & 20-Screen Engineering Decision |
| **Q6** | Producer-Consumer Design, Guarded Loops & `notifyAll()` | Deadlock Reproduction, 4 Conditions & Lock Ordering | TODO 1 & 2 Solutions & `ArrayBlockingQueue` Analysis |
| **Q7** | Stage 2: Concurrency Architecture & Collections Design | Stage 3: Injected Failures & 1/2/4/8-Thread Benchmarks | Stages 1 & 4 + Parallel Streams Evaluation |

