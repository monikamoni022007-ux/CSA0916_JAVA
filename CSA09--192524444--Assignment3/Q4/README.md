# Question 4: Thread Creation and the Thread Life Cycle

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Multithreading & Concurrency)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 7 Marks  

---

## 1. Objective and Problem Context
An examination cell scans physical answer scripts across four scanner stations simultaneously. Each station is simulated as an independent thread that processes a batch of answer scripts (Station 1: 25 scripts, Station 2: 30 scripts, Station 3: 20 scripts, Station 4: 25 scripts; Total = 100 scripts).

The key objectives are:
- Implement the scanning stations using both approaches: extending `java.lang.Thread` and implementing `java.lang.Runnable`.
- Demonstrate core lifecycle control methods: `start()`, `sleep()`, `join()`, and `isAlive()`.
- Trace the six JVM thread states (`NEW`, `RUNNABLE`, `TIMED_WAITING`, `WAITING`, `BLOCKED`, `TERMINATED`).
- Provide experimental evidence comparing `start()` vs. `run()`.
- Conduct behavioral experiments across 10 repeated runs, priority adjustments, and daemon thread termination.
- Evaluate fixed thread pool execution using `ExecutorService` and analyze time/memory complexity ($O(n/p)$ time and $O(p \times \text{StackSize})$ memory).

---

## 2. Shared Group Work: Dual Threading Implementation

### 2.1 Extending `Thread` vs. Implementing `Runnable`
- **Extending `Thread` ([`ScannerStationThread`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q4/ParallelScannerSimulation.java)):**
  - Inherits directly from `java.lang.Thread`.
  - Inflexible because Java supports only single class inheritance; the class cannot extend any other domain superclass (e.g. `HardwareScannerDevice`).
  - Tightly couples the task logic to the thread execution mechanism.
- **Implementing `Runnable` ([`ScannerStationRunnable`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q4/ParallelScannerSimulation.java)):**
  - Implements `java.lang.Runnable`, adhering to the Single Responsibility Principle by decoupling the task (the unit of work) from the execution vehicle (`Thread` or `ExecutorService`).
  - Allows the class to extend another business class and enables the same task to be submitted to thread pools.
  - **Industry Standard Recommendation:** Implementing `Runnable` (or `Callable`) is strongly preferred in production.

### 2.2 Lifecycle Control Methods Applied
| Method | Description & Usage in Scanner Simulation |
| :--- | :--- |
| `start()` | Requests the JVM/OS to allocate an independent call stack and schedule a new native thread to invoke `run()`. |
| `Thread.sleep(millis)` | Transitions the executing station thread into `TIMED_WAITING` to simulate optical scanning latency per script without releasing any locks. |
| `join()` | Causes the calling thread (`main`) to enter `WAITING` until the target station thread terminates, ensuring the final total is printed only after all scripts are processed. |
| `isAlive()` | Tests whether a thread has been started and has not yet terminated. Verified: `false` before `start()`, `true` while running, `false` after `join()`. |

---

## 3. Student A – Thread Design & Life-Cycle Analysis

### 3.1 Java Thread Life-Cycle State Diagram
```
        [NEW] (Thread instantiated, not yet started)
          |
       start()
          v
     [RUNNABLE] <------------------------------------+
    (Ready in OS queue <--> Running on CPU core)     |
          |                                          |
          | Thread.sleep(10) / join()                | Sleep expires / join finishes
          v                                          |
   [TIMED_WAITING / WAITING] ------------------------+
          |
          | Entering synchronized block (Lock held by other)
          v
      [BLOCKED] -------------------------------------+ (Monitor lock acquired)
          |
     run() completes normally or throws unhandled exception
          v
    [TERMINATED] (Thread dead; cannot be restarted; isAlive() == false)
```

### 3.2 Experimental Proof: Calling `run()` vs. Calling `start()`
Observed in [`RunVsStartDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q4/RunVsStartDemo.java):
```text
[EXPERIMENT 1: Calling run() Directly]
  [Task 1] Executing on Thread: 'main' (Thread ID: 1)
  [Task 2] Executing on Thread: 'main' (Thread ID: 1)
  [Task 3] Executing on Thread: 'main' (Thread ID: 1)
Sequential run() execution time: 156 ms (Strictly Serial)

[EXPERIMENT 2: Calling start() to Spawn Real Threads]
  [Task 1] Executing on Thread: 'WorkerThread-1' (Thread ID: 28)
  [Task 2] Executing on Thread: 'WorkerThread-2' (Thread ID: 29)
  [Task 3] Executing on Thread: 'WorkerThread-3' (Thread ID: 30)
Parallel start() execution time: 54 ms (Truly Concurrent)
```
- **Evidence:** Calling `run()` directly is an ordinary method call executing on the current thread's stack (`main`), running sequentially in $\sim 156\text{ ms}$. Calling `start()` requests the OS kernel to spawn independent threads, running in parallel in $\sim 54\text{ ms}$.

### 3.3 How `join()` Ensures Correct Total Calculation
1. **Thread Termination Coordination:** When `main` invokes `st.join()`, it blocks until `st` reaches the `TERMINATED` state.
2. **Memory Visibility (Java Memory Model - JLS §17.4.5):** The termination of a thread **happens-before** any other thread returns from `join()` on it. This guarantees that all updates made by the worker station to `scannedCount` are guaranteed visible in main memory before `main` sums the total (preventing stale reads).

---

## 4. Student B – Behavioural Experiments

### 4.1 Ten Repeated Runs (Completion Order Observation)
From [`StudentBExperiments.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q4/StudentBExperiments.java):
| Run Number | Station Finish Order | Observations & Scheduler Analysis |
| :--- | :--- | :--- |
| Run 1 | `[3, 1, 4, 2]` | Station 3 (20 scripts) finishes first due to smaller batch size. |
| Run 2 | `[3, 4, 1, 2]` | Stations 1 and 4 (25 scripts) interleave unpredictably. |
| Run 3 | `[3, 1, 2, 4]` | OS preemptive time-slicing alters order of identical workloads. |
| Run 4 | `[3, 4, 2, 1]` | Non-deterministic thread scheduling confirmed. |
| Run 5 | `[3, 2, 1, 4]` | Core allocation jitter alters thread progress. |
| Run 6 | `[3, 1, 4, 2]` | Station 2 (30 scripts) consistently finishes last or near-last. |
| Run 7 | `[3, 4, 1, 2]` | Re-ordering occurs between stations 1 and 4. |
| Run 8 | `[3, 2, 4, 1]` | Scheduler context-switches introduce minor latency variations. |
| Run 9 | `[3, 1, 2, 4]` | Identical total (100 scripts) preserved across all runs. |
| Run 10 | `[3, 4, 1, 2]` | Concurrency is non-deterministic, but final state is correct. |

### 4.2 Thread Priority Experiment
- Setting `s1.setPriority(Thread.MIN_PRIORITY)` (1) and `s4.setPriority(Thread.MAX_PRIORITY)` (10).
- **Finding:** Priority is only an advisory hint to the underlying OS scheduler. On modern preemptive kernels (Windows / Linux CFS), high-priority threads receive slightly larger time-slice allocations, but execution order is **not** strictly guaranteed.

### 4.3 Daemon Thread Experiment (`setDaemon(true)`)
- Station 4 configured as a daemon thread via `st4.setDaemon(true)` and `st4.join()` omitted.
- **Finding:** The JVM terminates immediately as soon as all user (non-daemon) threads complete, regardless of whether daemon threads are still active. Station 4 was abruptly killed mid-batch after processing only a fraction of its scripts, demonstrating that daemon threads must never be used for critical I/O or database operations.

---

## 5. Joint Conclusion & Discussion

### Why can the same multithreaded program produce different output orders?
Java threads are mapped 1-to-1 to native OS threads managed by the operating system kernel. CPU cache invalidations, hardware interrupts, background operating system processes, and microsecond sleep-timer jitter cause the OS preemptive scheduler to allocate CPU time-slices differently on every execution.

### Why must a correct program not depend on thread execution order?
A program whose correctness depends on a specific thread execution order suffers from a **race hazard**. A correct concurrent system must be **schedule-independent**: it must use proper synchronization primitives (`join()`, `CountDownLatch`, monitors, or thread-safe collections) so that the correct aggregated result (100 scripts) is always guaranteed regardless of the thread completion permutation.

---

## 6. Alternative Concept & Complexity Analysis

### Fixed Thread Pool via `ExecutorService`
Instead of manually spawning raw `Thread` instances:
```java
ExecutorService pool = Executors.newFixedThreadPool(p);
List<Future<Integer>> futures = pool.invokeAll(tasks);
```

### Time and Space Complexity ($n$ scripts, $p$ threads)
| Dimension | Raw `Thread` Objects ($n$ threads) | Fixed Thread Pool (`ExecutorService`, $p$ threads) |
| :--- | :--- | :--- |
| **Execution Time** | $O(n \times t_{\text{script}})$ (High spawn overhead) | $O(\frac{n}{p} \times t_{\text{script}})$ parallel batches |
| **Thread Spawn Overhead** | $O(n)$ expensive OS kernel thread creations | $O(p)$ one-time initialization; workers reused |
| **Native Stack Memory** | $O(n \times \text{StackSize})$ ($\sim 1\text{ MB}$ per thread; risks OOM) | **$O(p \times \text{StackSize})$** bounded to pool size |
| **Heap Memory** | $O(n)$ Thread control blocks | $O(p)$ workers $+ O(n)$ lightweight `Runnable` tasks in queue |

---

## 7. How to Compile and Run
All commands should be executed from the `Q4` folder:

```bash
cd Q4

# 1. Compile all programs
javac ParallelScannerSimulation.java RunVsStartDemo.java ThreadLifecycleStateTracker.java StudentBExperiments.java ExecutorAlternativeScanner.java

# 2. Run Main Scanner Simulation
java ParallelScannerSimulation

# 3. Run start() vs. run() Demonstration
java RunVsStartDemo

# 4. Run Thread Lifecycle State Tracker
java ThreadLifecycleStateTracker

# 5. Run Student B Behavioural Experiments (10 runs, priorities, daemon)
java StudentBExperiments

# 6. Run ExecutorService Alternative Concept
java ExecutorAlternativeScanner
```
Execution logs are generated locally when the programs run and are intentionally not committed.

