# Question 7: Design a Concurrent, Fault-Tolerant Processor from First Principles

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Concurrent Collections & Executor Framework)  
**Bloom's Taxonomy Level:** K5 – Evaluate  
**Marks:** 10 Marks  

---

## 1. Stage 1 – Interpret the Problem (Joint | 2 Marks)

### 1.1 Problem Context
At the end of each academic semester, the university examination cell processes batches of **10,000 answer-script score files**.
Each file contains:
- Line 1: Student Registration ID (e.g. `REG_0001`)
- Subsequent lines: Subject marks (integers, 0 to 100)

The system must validate each score, sum the total, and generate an institutional summary report.
Operational challenges:
- Certain files are missing on disk.
- Certain files contain non-numeric corrupt text.
- Certain files contain out-of-range marks ($< 0$ or $> 100$).
- Single file failures must **never** abort the entire batch run.
- A complete and accurate accounting of all successes and failures must be guaranteed.

### 1.2 Parallel Tasks vs. Shared Mutable State
- **Independent Parallel Tasks:** Opening, reading, parsing, validating, and calculating totals for each individual score file ($\text{File}_i$) has zero data dependency on any other file and executes concurrently in parallel.
- **Shared Mutable State:**
  - `successMap`: Stores valid student results (`StudentID` $\rightarrow$ `TotalScore`).
  - `failureLog`: Collects audit trail records for failed files with precise error causes.
  - `successCount` and `failureCount`: Global progress counters.

### 1.3 Measurable Engineering Requirements
| Requirement ID | Measurable Specification | Verification Metric |
| :--- | :--- | :--- |
| **REQ-01 (Completeness)** | Exactly 100% of input files must be accounted for. | $\text{SuccessCount} + \text{FailureCount} == 10,000$ |
| **REQ-02 (Fault Isolation)** | 0 unhandled thread crashes or batch aborts. | A missing or corrupt file in $\text{Task}_i$ has zero effect on $\text{Task}_j$. |
| **REQ-03 (Thread Safety)** | Zero race conditions or lost updates. | Verified with lock-free atomic collections under concurrent load. |
| **REQ-04 (Throughput)** | Scalable performance across thread counts. | Multi-threaded pools ($p=4, 8$) achieve sub-second processing. |

---

## 2. Stage 2 – Design (Student A | 3 Marks)

### 2.1 Concurrency Architecture
```
[10,000 Score Files] ---> Submit 10,000 ScoreFileTask (Callable<Boolean>)
                               |
                               v
        [ExecutorService FixedThreadPool (p = 1, 2, 4, 8 Threads)]
         +-- Try-with-resources reads & validates each file independently
         +-- Valid File   --> ConcurrentHashMap<String, Integer> + AtomicInteger successCount
         +-- Corrupt/Miss --> ConcurrentLinkedQueue<String>      + AtomicInteger failureCount
                               |
                               v
        [Main Thread iterates Future<Boolean>.get() & prints Consolidated Summary]
```

### 2.2 Class Diagram
```
+---------------------------------+      implements      +----------------------------+
| <<interface>> Callable<Boolean> | <------------------- |       ScoreFileTask        |
+---------------------------------+                      +----------------------------+
| + call(): Boolean               |                      | - filePath: Path           |
+---------------------------------+                      | - successMap: ConcurrentMap|
                                                         | - failureLog: ConcurrentQ  |
+---------------------------------+      creates         +----------------------------+
|    SemesterResultProcessor      | -------------------> | + call(): Boolean          |
| + executeBatch(files, p): long  |                      +----------------------------+
+---------------------------------+
```

### 2.3 Concurrent Collection Selection & Justification
1. **`ConcurrentHashMap<String, Integer>` (Valid Results):**
   - *Why not `Hashtable` or `Collections.synchronizedMap()`?* Legacy synchronized maps lock the entire table on every write, severely bottlenecking multi-threaded throughput.
   - *Advantage:* `ConcurrentHashMap` uses lock-free Compare-And-Swap (CAS) for empty bins and fine-grained per-bin node locking, allowing $p$ worker threads to insert results concurrently with $O(1)$ non-blocking speed.
2. **`ConcurrentLinkedQueue<String>` (Failure Audit Log):**
   - Implements the Michael-Scott non-blocking queue algorithm using atomic CAS pointers. Provides wait-free, thread-safe appending of failure strings without lock contention.
3. **`AtomicInteger` (Global Metrics):**
   - Uses CPU-level lock-free CAS instructions (`incrementAndGet()`), eliminating race conditions on primitive integer counters.

### 2.4 Fault Tolerance & Exception Handling Strategy
- **Two-Tier Exception Defense:**
  - *Tier 1 (Task Level):* Inside `ScoreFileTask.call()`, the entire file operation is wrapped in a `try-catch(Exception ex)`. Any `IOException`, `NumberFormatException`, or `IllegalArgumentException` is trapped locally, logged in `failureLog`, and returns `false`. The worker thread never crashes.
  - *Tier 2 (Batch Level):* The main thread awaits each `Future<Boolean>` inside a `try-catch(ExecutionException ex)` to capture any unexpected JVM runtime exceptions.

### 2.5 Pseudocode
```text
ALGORITHM: ProcessSemesterResults(filePaths[1..N], poolSize p)
1. Initialize ExecutorService pool = Executors.newFixedThreadPool(p)
2. Initialize ConcurrentHashMap<String, Integer> validResults
3. Initialize ConcurrentLinkedQueue<String> failureAuditLog
4. Initialize AtomicInteger successCount = 0, failureCount = 0
5. Initialize List<Future<Boolean>> futures = new ArrayList<>(N)
6. FOR EACH path IN filePaths DO:
     Submit Callable task to pool:
       a. Open file using try-with-resources (BufferedReader)
       b. Parse StudentID and Marks; validate 0 <= mark <= 100
       c. Put (StudentID, sum) into validResults; return TRUE
       d. CATCH (Exception ex):
          Add (path + " -> " + ex.getMessage()) to failureAuditLog; return FALSE
7. FOR EACH future IN futures DO:
     TRY: future.get()
     CATCH (ExecutionException ex): record unexpected runtime failure in failureAuditLog
8. Call pool.shutdown() and pool.awaitTermination()
9. Assert validResults.size() + failureAuditLog.size() == N
10. Print Summary Report
```

---

## 3. Stage 3 – Implementation and Measurement (Student B | 3 Marks)

### 3.1 Hand-Checked Verification Set (10 Files)
- Tested with 7 valid files, 1 corrupt text file, 1 out-of-range mark file, and 1 missing file.
- **Expected:** Total = 10, Success = 7, Failed = 3
- **Observed:**
  ```text
  Pool Size: 4 | Total: 10 | Success: 7 | Failed: 3 | Time: 14 ms
  ```
- *100% verification accuracy confirmed.*

### 3.2 10,000-File Benchmark Results (Thread Pools 1, 2, 4, 8)
Dataset: 10,000 files (9,960 valid, 20 corrupt, 20 missing).

| Pool Size ($p$) | Total Files | Succeeded | Failed | Execution Time (ms) | Speedup vs. $p=1$ |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1 Thread** (Sequential Baseline) | 10,000 | 9,960 | 40 | $\sim 250\text{--}350\text{ ms}$ | $1.00\times$ (Baseline) |
| **2 Threads** | 10,000 | 9,960 | 40 | $\sim 150\text{--}200\text{ ms}$ | $\sim 1.6\times\text{--}1.8\times$ |
| **4 Threads** | 10,000 | 9,960 | 40 | $\sim 100\text{--}140\text{ ms}$ | $\sim 2.3\times\text{--}2.8\times$ |
| **8 Threads** | 10,000 | 9,960 | 40 | $\sim 80\text{--}120\text{ ms}$ | $\sim 2.8\times\text{--}3.5\times$ |

*All 40 injected failures (20 missing, 20 corrupt) were accurately trapped and recorded without crashing any thread.*

---

## 4. Stage 4 – Defend the Design (Joint | 2 Marks)

### 4.1 Recommended Pool Size & Why More Threads Do Not Always Help
- **Recommended Pool Size:** We recommend $p = 4\text{ to }8$ worker threads (matching the physical core count: `Runtime.getRuntime().availableProcessors()`).
- **Why More Threads Do Not Always Help (Diminishing Returns):**
  1. **OS File-System & Disk I/O Saturation:** All worker threads read from the same underlying storage device. Storage controllers, disk queues, and OS Virtual File System (VFS) directory lock tables serialize read requests.
  2. **Context-Switching Overhead:** Increasing threads beyond physical CPU cores causes frequent OS kernel thread context switching and CPU L1/L2 cache invalidation.
  3. **Amdahl's Law:** The sequential fractions of the program (submitting 10,000 tasks to the executor queue and collecting `Future.get()` results) cannot be parallelized.

### 4.2 Correctness & Completeness Guarantee
1. **Future Tracking:** Every submitted file produces a `Future<Boolean>`. Iterating through all futures guarantees that the main thread does not proceed until every file has finished processing.
2. **Atomic Accounting:** Every task increments either `successCount` or `failureCount` before returning. Completeness is verified by:
   $$\text{successCount} + \text{failureCount} == 10,000$$
3. **Memory Visibility:** The Java Memory Model ensures that all mutations to `ConcurrentHashMap` and `ConcurrentLinkedQueue` are visible across threads via volatile read/write semantics and internal happens-before relationships.

### 4.3 Remaining System Limitation
- **In-Memory Result Accumulation:** All 10,000 results are held in JVM heap memory. If the dataset scales to 10,000,000 large files, heap memory exhaustion could occur.
- **Mitigation:** In production enterprise environments, results should be streamed directly to an append-only disk buffer or written into a relational database via JDBC batch inserts.

---

## 5. Alternative Concept & Complexity Analysis

### Comparison: FixedThreadPool vs. Parallel Streams vs. One-Thread-Per-File
| Architectural Approach | Time Complexity ($N$ files, $p$ threads) | Thread Stack Memory ($T \times \text{StackSize}$) | Failure Isolation & Resource Safety |
| :--- | :--- | :--- | :--- |
| **`ExecutorService` (Fixed Pool $p$)** | $O(\frac{N}{p} \times t_{\text{file}})$ | **$O(p \times 1\text{ MB})$** (e.g. 8 MB for $p=8$) | **High:** Dedicated pool; customizable queue; full `Future` lifecycle control. |
| **Parallel Streams (`parallelStream()`)** | $O(\frac{N}{C} \times t_{\text{file}})$ ($C$ = CPU cores) | $(C - 1) \times 1\text{ MB}$ | **Poor:** Monopolizes the JVM-wide shared `ForkJoinPool.commonPool()`, blocking other application tasks during file I/O! |
| **One Thread per File (10,000 Threads)** | High thread spawn overhead | **$10,000 \times 1\text{ MB} \approx 10\text{ GB}$ native RAM!** | **Dangerous:** Leads directly to `OutOfMemoryError: unable to create native thread`. |

---

## 6. How to Compile and Run
All commands should be executed from the `Q7` folder:

```bash
cd Q7

# 1. Compile all programs
javac SemesterResultProcessor.java ParallelStreamAlternative.java

# 2. Run the main 10,000-file Result Processor Benchmark (pools 1, 2, 4, 8)
java SemesterResultProcessor

# 3. Run the Alternative Concept (Parallel Streams vs One-Thread-Per-File)
java ParallelStreamAlternative
```
Benchmark logs are generated locally when the program runs and are intentionally not committed.

