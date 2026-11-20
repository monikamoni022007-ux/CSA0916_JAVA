# Question 6: Deadlock and Inter-Thread Communication

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Deadlock, Synchronization & Thread Coordination)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 7 Marks  

---

## 1. Objective and Problem Context
This task addresses two fundamental challenges in concurrent systems:
- **Part A (Hostel Wallet Transfers):** Students transfer money between hostel wallet accounts where each transaction locks both the sender and recipient wallets. Opposite locking orders can trigger a permanent circular-wait deadlock.
- **Part B (Mess Token Counter):** A hostel mess cook (producer) prepares meal tokens and places them on a serving counter of bounded capacity (max 5 tokens), while students (consumers) collect tokens. Coordinated using guarded `wait()`, `notify()`, and `notifyAll()` calls.

The core objectives are:
- Complete **TODO 1** and **TODO 2** in [`TokenCounter.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q6/TokenCounter.java) and verify the exact expected sum of **210** across 20 tokens.
- Analyze the difference between checking conditions with `while` versus `if`.
- Reproduce the 4 Coffman deadlock conditions in hostel wallet transfers and eliminate them using global lock ordering.
- Trace the bounded buffer size over time to prove $0 \le \text{count} \le 5$ at every microsecond.
- Evaluate alternative modern abstractions (`ArrayBlockingQueue` and `ReentrantLock.tryLock()`).

---

## 2. Complete the Code Solution (`TokenCounter.java`)

### Code Solution
```java
public synchronized void put(int token) throws InterruptedException {
    // TODO 1: Wait while the counter is full, add the token at position in, then
    // wake waiting threads
    while (count == buffer.length) {
        wait();
    }
    buffer[in] = token;
    in = (in + 1) % buffer.length;
    count++;
    notifyAll();
}

public synchronized int take() throws InterruptedException {
    // TODO 2: Wait while the counter is empty, remove the token at position out,
    // wake waiting threads and return the token
    while (count == 0) {
        wait();
    }
    int token = buffer[out];
    out = (out + 1) % buffer.length;
    count--;
    notifyAll();
    return token;
}
```

### Verification Against Expected Output
**Expected Output (from Assignment PDF Page 9):**
```text
Sum of tokens collected: 210 (expected 210)
```
**Actual Output:**
```text
Sum of tokens collected: 210 (expected 210)
```
*Exact mathematical match verified ($1 + 2 + \dots + 20 = \frac{20 \times 21}{2} = 210$).*

---

## 3. Student A – Producer-Consumer Design & Invariant Tracing

### 3.1 Why `wait()` Must Be Called Inside a `synchronized` Block and in a `while` Loop
1. **Inside a `synchronized` Block:** Calling `wait()` requires ownership of the object's monitor lock. If invoked without the monitor, the JVM immediately throws `IllegalMonitorStateException`. Holding the lock guarantees that checking the condition predicate (`count == 0`) and relinquishing the CPU to enter the wait-set is atomic, eliminating the **lost-wakeup** hazard.
2. **Inside a `while` Loop (Guarded Suspension):**
   - **Spurious Wakeups:** The underlying OS POSIX thread scheduler may wake a thread from `wait()` without any explicit `notify()` or signal.
   - **State Invalidation by Peer Threads:** When `notifyAll()` wakes multiple consumer threads, the first thread to acquire the lock consumes the item. When the second thread acquires the lock, the buffer is empty again. A `while` loop forces the thread to re-test the predicate and go back to waiting, whereas an `if` statement would allow it to consume from an empty buffer!

### 3.2 Experiment: Changing `while` to `if` ([`IfVsWhileTokenCounterDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q6/IfVsWhileTokenCounterDemo.java))
```text
Round  1: Sum collected = 205 (Expected: 210) | Final count = -1 <- ANOMALY / CORRUPTION!
Round  2: Sum collected = 210 (Expected: 210) | Final count =  0 <- ok
Round  3: Sum collected = 198 (Expected: 210) | Final count = -1 <- ANOMALY / CORRUPTION!
```
- **Finding:** With `if`, waiting consumer threads resume without re-verifying the predicate. When both students wake simultaneously, one student takes the last token, and the other student executes `take()` on an empty buffer, decrementing `count` to `-1` and reading corrupt data.

### 3.3 `notify()` vs. `notifyAll()`
With multiple consumers and producers sharing the same monitor wait-set, `notify()` wakes only **one arbitrary thread**. If a consumer consumes a token and calls `notify()`, the JVM might wake another *consumer* instead of the *cook*. The awakened consumer finds the buffer still empty and goes back to sleep. Because the cook was never notified, all threads end up permanently waiting in a **missed-signal deadlock**. `notifyAll()` ensures all waiting threads wake up and re-test their conditions.

### 3.4 Difference Between `sleep()` and `wait()`
| Feature | `Object.wait()` | `Thread.sleep(millis)` |
| :--- | :--- | :--- |
| **Class** | `java.lang.Object` (instance method) | `java.lang.Thread` (static method) |
| **Lock Release** | **RELEASES** the intrinsic monitor lock atomically while waiting. | **HOLDS** all acquired locks throughout the sleep duration. |
| **Context** | Must be called inside a `synchronized` block/method. | Can be called anywhere. |
| **Wakeup Mechanism** | Wakes via `notify()`, `notifyAll()`, or interrupt. | Wakes when timer expires or when interrupted. |

### 3.5 Bounded Buffer Invariant Tracing ($0 \le \text{count} \le 5$)
Verified in [`BoundedBufferTracingDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q6/BoundedBufferTracingDemo.java):
The buffer counter was tracked at every operation:
```text
  PUT  token= 1 | Buffer count=1
  PUT  token= 2 | Buffer count=2
  PUT  token= 3 | Buffer count=3
  TAKE token= 1 | Buffer count=2
  PUT  token= 4 | Buffer count=3
  ...
Did buffer size ever drop below 0 or exceed 5? -> NO (STRICTLY PRESERVED)
```

---

## 4. Student B – Deadlock Analysis and Prevention

### 4.1 The Four Coffman Conditions Present in Hostel Transfers
In [`HostelWalletDeadlockDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q6/HostelWalletDeadlockDemo.java):
1. **Mutual Exclusion:** Each wallet object has an intrinsic monitor that can be held by only one thread at a time.
2. **Hold and Wait:** Thread 1 holds the lock on Wallet 101 while waiting to acquire Wallet 102.
3. **No Preemption:** Neither thread can forcibly seize a lock from the other thread.
4. **Circular Wait:** Thread 1 (holding 101, waiting for 102) and Thread 2 (holding 102, waiting for 101) form a closed circular dependency cycle.

### 4.2 Consistent Lock Ordering Rule
In [`HostelWalletSafeTransfer.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q6/HostelWalletSafeTransfer.java):
```java
// Always acquire lock with smaller ID first
SafeWallet firstLock  = (from.walletId < to.walletId) ? from : to;
SafeWallet secondLock = (from.walletId < to.walletId) ? to : from;

synchronized (firstLock) {
    synchronized (secondLock) {
        from.balance -= amount;
        to.balance += amount;
    }
}
```

### 4.3 Why the Lock Ordering Rule Works
By ordering locks globally based on ascending `walletId`:
- Both Thread 1 (101 $\rightarrow$ 102) and Thread 2 (102 $\rightarrow$ 101) attempt to lock Wallet 101 first, and then Wallet 102.
- Whichever thread acquires Wallet 101 proceeds to acquire Wallet 102. The second thread waits on Wallet 101 before it can acquire *any* lock.
- **Proof:** Circular wait is mathematically impossible because no thread can ever hold a higher-numbered lock while requesting a lower-numbered lock. The resource allocation graph becomes a Directed Acyclic Graph (DAG).

---

## 5. Joint Conclusion & Discussion

### What do deadlock and the producer-consumer problem have in common?
Both are **thread liveness hazards** where threads enter a blocked or waiting state awaiting a condition or resource controlled by another thread.
- Deadlock represents a circular dependency hazard where threads block forever holding resources each other needs.
- The producer-consumer problem represents a resource-coordination hazard where threads must block temporarily until buffer capacity or data becomes available.

### How does each design ensure progress?
- **Lock Ordering:** Ensures progress by breaking the circular wait condition, ensuring that locks are acquired in a strict linear order without cycles.
- **Guarded `wait()`/`notifyAll()`:** Ensures progress by releasing the monitor lock while waiting (avoiding hold-and-wait while idle) and waking waiting threads as soon as buffer state changes.

---

## 6. Alternative Concept & Complexity Analysis

### `ArrayBlockingQueue` & `ReentrantLock.tryLock()`
- **Part B (`ArrayBlockingQueue<Integer>(5)`):** Replaces low-level `wait()`/`notifyAll()` with a bounded concurrent queue. Encapsulates two separate `Condition` queues (`notEmpty` and `notFull`), completely eliminating lost-wakeup and spurious-wakeup risks.
- **Part A (`tryLock(timeout, TimeUnit)`):** If a thread cannot acquire the second wallet lock within 50 ms, it releases its first lock and backs off, eliminating deadlock through timed preemption.

### Asymptotic Time and Space Complexity
| Dimension | Low-Level `wait()` / `notifyAll()` | `ArrayBlockingQueue` | Consistent Lock Ordering | `tryLock()` with Timeout |
| :--- | :--- | :--- | :--- | :--- |
| **Time per Op** | $O(1)$ amortized | $O(1)$ amortized | $O(1)$ direct lock | $O(1)$ or timeout duration |
| **Space Used** | $O(\text{capacity})$ array | $O(\text{capacity})$ array | $O(1)$ lock references | $O(1)$ lock references |
| **Error-Proneness** | **High:** Easy to miss `while`, `synchronized`, or `notifyAll()` | **Low:** Handled internally by JDK | **Low:** Simple deterministic ordering | **Medium:** Requires retry/back-off loop |

---

## 7. How to Compile and Run
All commands should be executed from the `Q6` folder:

```bash
cd Q6

# 1. Compile all programs
javac TokenCounter.java IfVsWhileTokenCounterDemo.java HostelWalletDeadlockDemo.java HostelWalletSafeTransfer.java BoundedBufferTracingDemo.java AlternativeQueueAndTryLock.java

# 2. Run Complete the Code program (TODO 1 & 2)
java TokenCounter

# 3. Run while vs. if demonstration
java IfVsWhileTokenCounterDemo

# 4. Run Deadlock-Free Lock-Ordered Transfers
java HostelWalletSafeTransfer

# 5. Run Bounded Buffer Invariant Tracing
java BoundedBufferTracingDemo

# 6. Run Alternative Concept (ArrayBlockingQueue & tryLock)
java AlternativeQueueAndTryLock

# 7. Run Deadlock Demonstration (terminates cleanly via watchdog)
java HostelWalletDeadlockDemo
```
Execution logs are generated locally when the programs run and are intentionally not committed.

