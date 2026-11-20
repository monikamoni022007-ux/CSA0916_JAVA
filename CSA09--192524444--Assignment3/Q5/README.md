# Question 5: Race Conditions in a Cinema Seat-Booking System

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Multithreading & Synchronization)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 6 Marks  

---

## 1. Objective and Problem Context
A multiplex cinema ticketing application allows 100 concurrent customer threads to reserve seats simultaneously across 10 available seats (10 users competing for each seat index).
Each transaction checks if a seat is free (`!booked[seat]`) and, if available, marks it as booked (`booked[seat] = true`) and increments `bookingsMade`.

Without synchronization, this **check-then-act** sequence is non-atomic:
- Multiple threads read `booked[seat] == false` simultaneously.
- All competing threads pass the condition and overwrite `booked[seat] = true`.
- Consequently, the same physical seat is sold multiple times (double-booking race hazard), and `bookingsMade` exceeds the 10 available seats!

The objective is to:
- Complete **TODO 1** in [`SeatBooking.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q5/SeatBooking.java) to make the check-then-book operation atomic.
- Demonstrate the unsynchronized race condition across 10 repeated runs.
- Compare synchronized methods against synchronized blocks for 10,000 bookings.
- Evaluate the 20-screen multiplex engineering decision.
- Analyze lock-free alternatives (`AtomicBoolean[]`, `ReentrantLock`, `ConcurrentHashMap`).

---

## 2. Complete the Code Solution (`SeatBooking.java`)

### Code Solution
In [`SeatBooking.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q5/SeatBooking.java), completing **TODO 1**:
```java
// TODO 1: Make the check-then-book operation atomic so that a seat can never be
// sold twice
public synchronized void book(int seat) {
    if (!booked[seat]) {
        booked[seat] = true;
        bookingsMade++;
    }
}
```

### Verification Against Expected Output
**Expected Output from Assignment PDF Page 7:**
```text
Seats sold: 10
```
**Actual Output from Execution:**
```text
Seats sold: 10
```
*Exact match confirmed.*

---

## 3. Student A – Producing and Explaining the Race

### 3.1 Why the Unsynchronized Version May Sell More Than 10 Seats
The statement block:
```java
if (!booked[seat]) {
    booked[seat] = true;
    bookingsMade++;
}
```
consists of multiple non-atomic bytecode instructions:
1. `aload_0`, `getfield #booked`, `iload_1`, `baload` (Read `booked[seat]`)
2. `ifne` (Conditional branch check)
3. `aload_0`, `getfield #booked`, `iload_1`, `iconst_1`, `bastore` (Write `booked[seat] = true`)
4. `aload_0`, `dup`, `getfield #bookingsMade`, `iconst_1`, `iadd`, `putfield #bookingsMade` (Read-Modify-Write counter)

### 3.2 Thread Interleaving Diagram (Step-by-Step Race Condition)
```
Time   Thread 0 (User A, Seat 0)               Thread 10 (User B, Seat 0)
─────────────────────────────────────────────────────────────────────────────
t1     Read booked[0] -> false
t2                                             Read booked[0] -> false (STALE!)
t3     Passes if (!booked[0]) condition
t4                                             Passes if (!booked[0]) condition
t5     Writes booked[0] = true
t6                                             Writes booked[0] = true (OVERWRITE!)
t7     bookingsMade++ (count = 1)
t8                                             bookingsMade++ (count = 2!)
─────────────────────────────────────────────────────────────────────────────
Result: Seat 0 is sold TWICE! bookingsMade incremented twice for one physical seat.
```

### 3.3 The Exact Critical Section
The exact critical section is the compound read-modify-write block accessing shared mutable state:
```java
if (!booked[seat]) {
    booked[seat] = true;
    bookingsMade++;
}
```
Atomicity must cover **both** reading/writing the `booked` array and updating `bookingsMade`.

### 3.4 Unsynchronized 10-Run Reproduction Results ([`UnsynchronizedSeatBooking.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q5/UnsynchronizedSeatBooking.java))
```text
  Run  1: Seats sold = 13 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  2: Seats sold = 14 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  3: Seats sold = 14 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  4: Seats sold = 14 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  5: Seats sold = 15 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  6: Seats sold = 14 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  7: Seats sold = 18 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  8: Seats sold = 15 / 10 <- DOUBLE BOOKING OCCURRED!
  Run  9: Seats sold = 24 / 10 <- DOUBLE BOOKING OCCURRED!
  Run 10: Seats sold = 15 / 10 <- DOUBLE BOOKING OCCURRED!
```
**Finding:** In 100% of test runs with slight scheduling interleaving, the unsynchronized version suffered severe double bookings, selling up to **24 seats** out of 10 available seats!

---

## 4. Student B – Comparing Synchronization Strategies

### 4.1 10,000 Bookings Performance Benchmark ([`CinemaSyncComparison.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q5/CinemaSyncComparison.java))
10,000 concurrent threads competing across 50 seats:
| Synchronization Strategy | Monitor Lock Acquired | Correctness (Seats Booked) | Measured Execution Time |
| :--- | :--- | :--- | :--- |
| **Synchronized Method** | Intrinsic monitor of `this` (`SeatBooking` instance) | Correct: Exactly 50 / 50 seats sold | $\sim 528\text{ ms}$ |
| **Synchronized Block** | Intrinsic monitor of `this` (critical section only) | Correct: Exactly 50 / 50 seats sold | $\sim 512\text{ ms}$ |

### 4.2 Whole Method vs. Critical Section Locking
- **Synchronized Method (`public synchronized void book`):** Holds the monitor lock for the entire duration of the method call. If the method performs parameter validation, calculations, or audit logging, all other threads are blocked waiting for the lock.
- **Synchronized Block (`synchronized(this) { ... }`):** Limits lock acquisition strictly to the shared mutable state update. Non-critical code (such as checking `seat < 0 || seat >= seats`) executes concurrently outside the lock, reducing lock hold time and contention.

### 4.3 Intrinsic Locks & Static Synchronization
- **Intrinsic Lock:** Every Java object has an internal monitor. An instance synchronized method (`synchronized void book()`) locks `this` (the individual `SeatBooking` object instance).
- **When is Static Synchronization Needed?**  
  Static synchronization (`public static synchronized void book()`) locks the `Class` object (`SeatBooking.class`) shared across the entire JVM. It would be needed **only** if the seats array or booking counters were declared `static` (e.g. `private static boolean[] globalSeats`). If used with multiple cinema screens, static synchronization would erroneously serialize bookings across completely different movies!

---

## 5. Joint Engineering Decision: Multiplex with 20 Screens

### Which synchronization strategy would you deploy for a cinema with 20 screens, and why?
**Engineering Decision:**  
We deploy **per-screen instance-level synchronization** (or per-seat lock arrays / `AtomicBoolean[]`), and **NEVER** use static synchronization or a global database lock.

**Justification:**
1. **Independent Failure & Contention Domains:** Screen 1 (e.g. *Avatar*) has zero data dependency on Screen 20 (e.g. *Oppenheimer*). Customers booking Screen 1 should never contend with or block customers booking Screen 20.
2. **Horizontal Concurrency:** Instance-level locking allows all 20 screens to process reservations simultaneously with **$20\times$ parallelism**.
3. **High Contention Resilience:** Within each individual screen, deploying fine-grained per-seat locks (`AtomicBoolean[]`) ensures that a user reserving Seat A1 does not block a user reserving Seat F10.

---

## 6. Alternative Concept & Complexity Analysis

### Alternative Implementations ([`AlternativeBookingTechniques.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q5/AlternativeBookingTechniques.java))
```java
// 1. AtomicBoolean[]: Lock-Free CAS
if (seats[seat].compareAndSet(false, true)) { bookingsMade.incrementAndGet(); }

// 2. ReentrantLock: Explicit Lock with try-finally
lock.lock();
try { if (!booked[seat]) { booked[seat] = true; bookingsMade++; } }
finally { lock.unlock(); }

// 3. ConcurrentHashMap: Lock-Free Bucket Coordination
seatReservations.putIfAbsent(seat, userId);
```

### Complexity Comparison ($s$ seats, $c$ contending threads per seat)
| Mechanism | Time per Booking Under Contention | Extra Space Complexity for $s$ Seats | Characteristics |
| :--- | :--- | :--- | :--- |
| **`synchronized` Method** | $O(c \cdot s)$ (All threads across all seats serialize on 1 monitor) | $O(s)$ boolean array $+ O(1)$ monitor | Simple, coarse-grained |
| **`AtomicBoolean[]` (CAS)** | **$O(c)$** for contested seat; **$O(1)$** for distinct seats | $O(s)$ `AtomicBoolean` objects ($\sim 16\text{ bytes}$ each) | Lock-free, non-blocking, zero deadlock risk |
| **`ReentrantLock`** | $O(c \cdot s)$ with single lock; $O(c)$ with per-seat locks | $O(s)$ boolean array $+ O(1)$ lock | Allows fairness policies & timed `tryLock()` |
| **`ConcurrentHashMap`** | $O(1)$ average via per-bucket striped synchronization | $O(s)$ Node entries + boxed `Integer` keys | Highly scalable; stores customer ID mapping |

---

## 7. How to Compile and Run
All commands should be executed from the `Q5` folder:

```bash
cd Q5

# 1. Compile all programs
javac SeatBooking.java UnsynchronizedSeatBooking.java CinemaSyncComparison.java AlternativeBookingTechniques.java

# 2. Run Complete the Code program (TODO 1 solved, outputs "Seats sold: 10")
java SeatBooking

# 3. Run Unsynchronized 10-run demonstration (proves double-booking race condition)
java UnsynchronizedSeatBooking

# 4. Run 10,000 bookings comparison benchmark
java CinemaSyncComparison

# 5. Run Alternative Concepts (AtomicBoolean, ReentrantLock, ConcurrentHashMap)
java AlternativeBookingTechniques
```
Execution logs are generated locally when the programs run and are intentionally not committed.
